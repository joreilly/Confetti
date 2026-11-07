package dev.johnoreilly.confetti.ui.livery

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import dev.johnoreilly.confetti.livery.Action
import dev.johnoreilly.confetti.livery.ConferenceSeries
import dev.johnoreilly.confetti.livery.ConfettiContract
import dev.johnoreilly.confetti.livery.ContractData
import dev.johnoreilly.confetti.livery.PromoContract
import dev.johnoreilly.confetti.livery.SduiExperiment
import dev.johnoreilly.confetti.ui.components.SlotTracker
import dev.johnoreilly.confetti.livery.materialThemeValues
import dev.johnoreilly.confetti.ui.LocalDynamicColorInUse
import ee.schimke.livery.client.ActionRouter
import ee.schimke.livery.client.Bindings
import ee.schimke.livery.client.Clock
import ee.schimke.livery.client.DecisionContext
import ee.schimke.livery.client.EventSink
import ee.schimke.livery.client.Experiments
import ee.schimke.livery.client.ImageResolver
import ee.schimke.livery.client.LocalBucketing
import ee.schimke.livery.client.LocalRulesDecider
import ee.schimke.livery.client.MemoryFrequencyStore
import ee.schimke.livery.client.PlanResult
import ee.schimke.livery.client.SlotPlanner
import ee.schimke.livery.client.TemplateQuery
import ee.schimke.livery.client.TemplateRepository
import ee.schimke.livery.compose.LiveryHost
import ee.schimke.livery.compose.LiverySlot
import ee.schimke.livery.protocol.NamedValue
import ee.schimke.livery.protocol.PlayerCapabilities
import ee.schimke.livery.protocol.Surfaces
import ee.schimke.livery.protocol.TemplateEvent
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * What a platform hands the shared UI to draw conference-styled slots. Absent (the default), every
 * slot draws Confetti's own UI. Built only for a conference in the [SduiExperiment.LIVERY] arm
 * (see `LiveryComponents`), so the native arm never constructs one.
 *
 * Each conference series is its own Livery catalog ([catalogs]): published to its own URL and
 * bundled into the app as its own copy, so one conference's designs, theme packs, promotions and
 * experiments ship (or break) without touching another's.
 */
class LiveryStyling(
    /** Series (`droidcon`, `kotlinconf`) → its catalog: downloaded when it can be, else bundled. */
    catalogs: Map<String, TemplateRepository>,
    val clock: Clock,
    /** `android`, `ios`, `desktop` or `web`: what placement rules and experiments target. */
    val platform: String,
    val locale: String,
    val appVersion: Long,
    /** A stable, app-scoped id for sticky rollouts and arms; never an account id. */
    val bucketingId: String,
    /** Every slot's events: Livery's analytics shape, a log, a test probe. */
    val events: EventSink = EventSink {},
    val images: (TemplateRepository) -> ImageResolver? = { null },
    val capabilities: PlayerCapabilities = InApp,
    /** Hosts an `open-url` action may open. */
    val urlAllowlist: Set<String> = setOf("droidcon.com", "kotlinconf.com", "jetbrains.com", "confetti-app.dev"),
    /** Whether slots ask each catalog's server for a newer catalog (at most every [refreshEveryMs]). */
    val refresh: Boolean = true,
    val refreshEveryMs: Long = 5 * 60_000L,
) {
    val catalogs: Map<String, SeriesCatalog> = catalogs.mapValues { (series, repo) -> SeriesCatalog(series, repo) }

    fun catalog(conference: String): SeriesCatalog? = ConferenceSeries.of(conference)?.let { catalogs[it] }

    fun context(conference: String, segments: Set<String> = emptySet()) = DecisionContext(
        nowEpochMs = clock.nowEpochMs(), platform = platform, locale = locale, appVersion = appVersion,
        bucketingId = bucketingId, segments = segments + ConferenceSeries.segment(conference),
    )

    /** Whether an `open-url` may go to [url]: https only, by the host it really reaches (Livery's own parser). */
    fun allowsUrl(url: String): Boolean = urlAllowed(url, urlAllowlist)

    /** One series' catalog and everything planned from it. */
    inner class SeriesCatalog(val series: String, val repository: TemplateRepository) {
        /**
         * The catalog's experiments, assigned on the device (the same arm on every platform for the
         * same [bucketingId]). Every slot event goes through [Experiments.sink], which adds the
         * arm's exposure the first time it draws.
         */
        val experiments = Experiments(
            manifest = { repository.current() },
            context = { context(conference = series) },
            assigner = LocalBucketing,
            events = events,
        )
        val planner = SlotPlanner(repository, experiments.sink, clock, experiments)
        val decider = LocalRulesDecider({ repository.current() }, MemoryFrequencyStore(), LocalBucketing)
        val imageResolver: ImageResolver? = images(repository)

        private val refreshLock = Mutex()
        private var lastRefreshEpochMs = Long.MIN_VALUE / 2

        /**
         * Asks the server for a newer catalog, at most every [refreshEveryMs]. The bundled copy has
         * serial 0, so anything downloaded wins; slots replan on the repository's revision, so a
         * server design replaces a bundled one while the screen is open.
         */
        suspend fun refreshIfDue() {
            if (!refresh) return
            refreshLock.withLock {
                val now = clock.nowEpochMs()
                if (now - lastRefreshEpochMs < refreshEveryMs) return
                lastRefreshEpochMs = now
            }
            try {
                repository.refresh()
            } catch (e: CancellationException) {
                // The slot that started it left composition mid-download (scrolled away): nothing
                // was refreshed, so let the next slot try now rather than in refreshEveryMs.
                lastRefreshEpochMs = Long.MIN_VALUE / 2
                throw e
            } catch (_: Exception) {
                // Offline or a bad catalog: the bundled or cached one keeps drawing until next time.
            }
        }

        fun theme(dark: Boolean): suspend () -> Bindings? = { repository.theme(ConferenceSeries.themePack(series, dark)) ?: repository.theme(series) }

        fun query(contract: ConfettiContract) =
            TemplateQuery(contract.ref, capabilities, tags = mapOf(ConferenceSeries.TAG to series), minor = contract.minor)

        /**
         * Events through [Experiments.sink] (an `Impression` reports a catalog experiment's
         * exposure once), each tagged `exp_sdui=livery`: the arm Firebase assigned this conference.
         */
        val slotEvents: EventSink = EventSink { e ->
            experiments.sink.emit(e.copy(experiments = e.experiments + (SduiExperiment.ID to SduiExperiment.LIVERY)))
        }
    }

    companion object {
        val InApp = PlayerCapabilities(Surfaces.IN_APP, maxRcApiLevel = 7, profiles = setOf("androidx"))
    }
}

/** [url] is https and the host it really reaches is one of [hosts] or under one of them. */
internal fun urlAllowed(url: String, hosts: Set<String>): Boolean {
    val host = ImageResolver.remoteHost(url) ?: return false
    return hosts.any { host == it || host.endsWith(".$it") }
}

/** What a slot does with each contract action, by the action itself (not a string). */
typealias ActionHandlers = Map<Action, suspend (args: Map<String, String>) -> Unit>

/**
 * One contract-typed slot for one conference: the conference's design when Livery has one that
 * plays here, otherwise [fallback] (Confetti's own composable), handed a [SlotTracker] that reports
 * its taps as `design_id=native`. [data] is the contract's own data class, so it can't carry a
 * field the designs don't know.
 */
@Composable
fun ConferenceSlot(
    styling: LiveryStyling,
    conference: String,
    data: ContractData,
    modifier: Modifier = Modifier.fillMaxWidth(),
    /** What the slot shows (a session id): a lazy list can reuse a slot for another item. */
    identity: Any? = null,
    actions: ActionHandlers = emptyMap(),
    /**
     * Confetti's own composable, drawn where there's no template. Null when Confetti draws nothing
     * there (the header): then nothing is shown and no impression is counted, as in the native arm.
     */
    fallback: (@Composable (SlotTracker) -> Unit)?,
) {
    val catalog = styling.catalog(conference) ?: return fallback?.invoke(SlotTracker.None) ?: Unit
    LaunchedEffect(catalog) { catalog.refreshIfDue() }
    val contract = data.contract
    val json = remember(data) { data.toJson() }
    val host = rememberHost(styling, catalog, actions)
    val colors = rememberSlotColors()
    // When this conference has no template for the slot, the composable draws, still tracked.
    val native = remember(catalog, contract) { nativeTracker(styling, catalog, contract) }
    // Re-planned when the data or the colours change, and on a refreshed catalog (LiverySlot
    // watches the repository's revision): a mode switch or a new wallpaper recolours live.
    LiverySlot(key = json to colors, host = host, modifier = modifier, fallback = { reason ->
        // Nothing to draw (the header): nothing to see, so no impression either.
        val draw = fallback ?: return@LiverySlot
        // A reason means there's no template here, so Confetti's row is what the user sees: its
        // impression counts, once per item, as the native arm's does. None (still planning) is
        // the brief placeholder before a template arrives, and isn't counted.
        if (reason != null) {
            LaunchedEffect(contract, identity) {
                catalog.slotEvents.emit(TemplateEvent(
                    TemplateEvent.Type.Impression, styling.clock.nowEpochMs(), designId = SduiExperiment.NATIVE_DESIGN, detail = contract.ref,
                ))
            }
        }
        draw(native)
    }) {
        catalog.planner.forAppData(catalog.query(contract), json, extra = colors.app ?: catalog.theme(colors.dark)() ?: Bindings.EMPTY)
    }
}

private fun nativeTracker(
    styling: LiveryStyling,
    catalog: LiveryStyling.SeriesCatalog,
    contract: ConfettiContract,
): SlotTracker = SlotTracker { action, args ->
    require(contract.actions[action.name] === action) { "${action.name} is not an action of ${contract.ref}" }
    // Only an argument the contract declares reaches analytics, as with a template (PROTOCOL §7).
    catalog.slotEvents.emit(TemplateEvent(
        TemplateEvent.Type.Action, styling.clock.nowEpochMs(), designId = SduiExperiment.NATIVE_DESIGN,
        action = action.name, detail = contract.ref, args = args.filterKeys { it == action.arg },
    ))
}

/**
 * A growth placement (`home.feed`, `session.footer`, `speaker.footer`): whatever the conference
 * catalog's rules decide for its attendees right now. Draws nothing when nothing is decided or
 * when it is dismissed.
 */
@Composable
fun PromoSlot(
    styling: LiveryStyling,
    placement: String,
    conference: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    segments: Set<String> = emptySet(),
) {
    val catalog = styling.catalog(conference) ?: return
    var dismissed by rememberSaveable(placement, conference) { mutableStateOf(false) }
    if (dismissed) return
    LaunchedEffect(catalog) { catalog.refreshIfDue() }
    val uriHandler = LocalUriHandler.current
    val host = rememberHost(styling, catalog, mapOf(
        PromoContract.ctaAction to { args -> args["url"]?.let(uriHandler::openUri) },
        PromoContract.dismiss to { _ -> dismissed = true },
    ))
    val colors = rememberSlotColors()
    LiverySlot(key = Triple(placement, conference, colors), host = host, modifier = modifier) {
        val decision = catalog.decider.decide(placement, styling.context(conference, segments))
            ?: return@LiverySlot PlanResult.Fallback("no decision for $placement")
        catalog.planner.forDecision(catalog.query(PromoContract), decision, extra = colors.app ?: catalog.theme(colors.dark)() ?: Bindings.EMPTY)
    }
}

/**
 * Whether the app around a slot is dark, and the user's Material 3 colours when dynamic colour is
 * in use ([LocalDynamicColorInUse]); read from the [MaterialTheme] the slot sits in, so the app's
 * own light/dark setting wins over the system's.
 */
private data class SlotColors(val dark: Boolean, val app: Bindings?)

@Composable
private fun rememberSlotColors(): SlotColors {
    val scheme = MaterialTheme.colorScheme
    val dynamic = LocalDynamicColorInUse.current
    return remember(scheme, dynamic) {
        SlotColors(dark = scheme.surface.luminance() < 0.5f, app = if (dynamic) scheme.toLiveryTheme() else null)
    }
}

/** A Material 3 scheme as the designs' theme roles (the catalog's renders use the same mapping). */
fun ColorScheme.toLiveryTheme(): Bindings {
    fun Color.argb() = toArgb().toLong() and 0xFFFFFFFFL
    return Bindings(materialThemeValues(
        primary = primary.argb(), onPrimary = onPrimary.argb(), secondary = secondary.argb(),
        tertiary = tertiary.argb(), surface = surfaceContainerHigh.argb(), onSurface = onSurface.argb(),
        onSurfaceVariant = onSurfaceVariant.argb(),
    ).mapValues { NamedValue.Color(it.value) })
}

/** Every action kind a Confetti contract declares; the router dispatches by kind (PROTOCOL §7). */
private val ActionKinds = listOf("app:open-session", "app:toggle-bookmark", "app:open-speaker", "open-url", "dismiss")

@Composable
private fun rememberHost(styling: LiveryStyling, catalog: LiveryStyling.SeriesCatalog, actions: ActionHandlers): LiveryHost {
    // The latest handlers, so a recomposition with new lambdas never rebuilds the host.
    val current by rememberUpdatedState(actions)
    return remember(styling, catalog) {
        val events = catalog.slotEvents
        LiveryHost(
            repository = catalog.repository,
            actions = ActionRouter(
                handlers = ActionKinds.associateWith { kind ->
                    { _, _, args -> current.entries.firstOrNull { it.key.kind == kind }?.value?.invoke(args) ?: Unit }
                },
                urlAllowlist = styling::allowsUrl,
                events = events,
                clock = styling.clock,
            ),
            events = events,
            clock = styling.clock,
            images = catalog.imageResolver,
        )
    }
}
