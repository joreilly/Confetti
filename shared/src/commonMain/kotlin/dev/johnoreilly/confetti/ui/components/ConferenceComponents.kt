package dev.johnoreilly.confetti.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.johnoreilly.confetti.analytics.AnalyticsEvent
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.livery.Action
import dev.johnoreilly.confetti.livery.ConferenceSeries
import dev.johnoreilly.confetti.livery.ConfettiContract
import dev.johnoreilly.confetti.livery.SduiExperiment
import dev.johnoreilly.confetti.livery.SessionContract
import dev.johnoreilly.confetti.ui.sessions.SessionItemView
import kotlinx.datetime.LocalDateTime

/**
 * Everything a screen draws that a conference can restyle: the agenda's header, its session cards
 * and the promotions placed around the app. The app's composables take it as a context parameter
 * (`context(components: ConferenceComponents)`) from `App()` down, so which implementation draws is
 * decided once, at each platform's root, and no screen looks it up:
 *
 * - [NativeComponents]: Confetti's own composables, the only one most builds ever use;
 * - `LiveryComponents` (the `:livery` module, sduiDebug only): the conference's Livery designs,
 *   falling back to native;
 * - [SduiComponents]: per conference, whichever its arm of the [SduiExperiment] says.
 *
 * Nothing here refers to Livery's types, so a conference in the native arm never loads it.
 */
interface ConferenceComponents {
    /** Above the agenda. Native draws nothing (the agenda as before). */
    @Composable
    fun ConferenceHeader(state: SessionsUiState.Success)

    /** One talk in the agenda. */
    @Composable
    fun SessionCard(card: SessionCardModel)

    /** A promotion at [placement] (`home.feed`, `session.footer`, `speaker.footer`). Native draws nothing. */
    @Composable
    fun Promo(placement: String, conference: String, modifier: Modifier)
}

/** What a session card needs, whoever draws it: the talk, its state and the agenda's callbacks. */
class SessionCardModel(
    val conference: String,
    val session: SessionDetails,
    val now: LocalDateTime,
    val isBookmarked: Boolean,
    val isLoggedIn: Boolean,
    val trackColor: Color?,
    val tags: List<String>,
    val sessionSelected: (String) -> Unit,
    val addBookmark: (String) -> Unit,
    val removeBookmark: (String) -> Unit,
    val onNavigateToSignIn: () -> Unit,
) {
    /** The bookmark star, as a design's single `bookmark` action does it. */
    fun toggleBookmark() = when {
        !isLoggedIn -> onNavigateToSignIn()
        isBookmarked -> removeBookmark(session.id)
        else -> addBookmark(session.id)
    }
}

/**
 * How a native composable reports what the user did, under the contract's action names: a tap on
 * Confetti's own session row is `open`, the same as a tap on a design's card, so both arms of the
 * [SduiExperiment] are counted alike. [None] when nothing is measured.
 */
fun interface SlotTracker {
    fun action(action: Action, args: Map<String, String>)

    fun action(action: Action) = action(action, emptyMap())

    companion object {
        val None = SlotTracker { _, _ -> }
    }
}

/**
 * Confetti's own composables. With [tracking], each card reports its impression and taps as the
 * same analytics events a Livery design does (`livery_impression`, `livery_action` with
 * `design_id=native` and `exp_sdui=<arm>`), without loading Livery.
 */
open class NativeComponents(
    private val tracking: SduiTracking? = null,
    /** The arm this instance draws for: native itself, or livery's fallback where it has no design. */
    private val arm: String = SduiExperiment.NATIVE,
) : ConferenceComponents {
    @Composable
    override fun ConferenceHeader(state: SessionsUiState.Success) {}

    @Composable
    override fun SessionCard(card: SessionCardModel) {
        val session = card.session
        val tracker = tracking?.tracker(card.conference, SessionContract, arm) ?: SlotTracker.None
        if (tracking != null) {
            // Once per session shown: a lazy list reuses a row for another session.
            LaunchedEffect(card.conference, session.id) { tracking.impression(card.conference, SessionContract, arm) }
        }
        SessionCard(card, tracker)
    }

    /** The row itself, reporting through [tracker]: also what Livery draws without a design. */
    @Composable
    fun SessionCard(card: SessionCardModel, tracker: SlotTracker) {
        SessionItemView(
            session = card.session,
            sessionSelected = { tracker.action(SessionContract.open); card.sessionSelected(it) },
            isBookmarked = card.isBookmarked,
            addBookmark = card.addBookmark,
            removeBookmark = card.removeBookmark,
            onNavigateToSignIn = card.onNavigateToSignIn,
            isLoggedIn = card.isLoggedIn,
            trackColor = card.trackColor,
            tags = card.tags,
            // Every tap, as a design's `bookmark` action is: signed out included (it then prompts).
            onBookmarkTap = { tracker.action(SessionContract.bookmark) },
        )
    }

    @Composable
    override fun Promo(placement: String, conference: String, modifier: Modifier) {}
}

/**
 * The user's arm of the [SduiExperiment] per conference series: `native` or `livery`. Decided
 * before anything is drawn (Firebase Remote Config on Android), so a native arm never builds
 * Livery. Null means the series isn't in the experiment: it draws [SduiExperiment.LIVERY].
 */
fun interface SduiArms {
    fun arm(series: String): String?
}

/**
 * Per conference, the implementation its arm names. The Livery side is built on first use, so a
 * session where every conference is native never constructs a repository, reads a bundled
 * catalog or touches the network for templates.
 */
class SduiComponents(
    private val arms: SduiArms,
    private val native: ConferenceComponents,
    livery: () -> ConferenceComponents,
) : ConferenceComponents {
    private val liveryLazy = lazy(livery)

    /** Whether the Livery side has been built (tests: the native arm must never build it). */
    val liveryStarted: Boolean get() = liveryLazy.isInitialized()

    /** The conference's arm: its series' (`sdui_droidcon`), or the base catalog's (`sdui_confetti`). */
    fun armFor(conference: String): String {
        val series = ConferenceSeries.of(conference) ?: ConferenceSeries.BASE
        return arms.arm(series) ?: SduiExperiment.LIVERY
    }

    private fun pick(conference: String): ConferenceComponents =
        if (armFor(conference) == SduiExperiment.LIVERY) liveryLazy.value else native

    @Composable
    override fun ConferenceHeader(state: SessionsUiState.Success) = pick(state.conference).ConferenceHeader(state)

    @Composable
    override fun SessionCard(card: SessionCardModel) = pick(card.conference).SessionCard(card)

    @Composable
    override fun Promo(placement: String, conference: String, modifier: Modifier) =
        pick(conference).Promo(placement, conference, modifier)
}

/**
 * The [SduiExperiment]'s events for what Confetti draws itself, in the shape Livery's
 * `AnalyticsEventSink` gives its own (`livery_<type>`, `design_id`, `action`, `exp_<id>`), so a
 * dashboard compares the arms on one set of names. Through Confetti's [AnalyticsLogger]: Firebase
 * Analytics in release, logcat in debug.
 */
class SduiTracking(private val analytics: AnalyticsLogger) {
    fun impression(conference: String, contract: ConfettiContract, arm: String) =
        log("livery_impression", conference, contract, arm, emptyMap())

    fun tracker(conference: String, contract: ConfettiContract, arm: String) = SlotTracker { action, args ->
        require(contract.actions[action.name] === action) { "${action.name} is not an action of ${contract.ref}" }
        // Only an argument the contract declares reaches analytics, as with a template (PROTOCOL §7).
        log("livery_action", conference, contract, arm, mapOf("action" to action.name) +
            args.filterKeys { it == action.arg }.mapKeys { "arg_${it.key}" })
    }

    private fun log(name: String, conference: String, contract: ConfettiContract, arm: String, extra: Map<String, String>) {
        analytics.logEvent(SduiEvent(name, buildMap {
            put("design_id", SduiExperiment.NATIVE_DESIGN)
            put("contract", contract.ref)
            put("conference", conference)
            put("exp_${SduiExperiment.ID}", arm)
            putAll(extra)
        }))
    }
}

/** One analytics event of the [SduiExperiment]. */
data class SduiEvent(override val id: String, override val properties: Map<String, Any>) : AnalyticsEvent
