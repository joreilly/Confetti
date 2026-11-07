package dev.johnoreilly.confetti.livery

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import dev.johnoreilly.confetti.ui.components.ConferenceComponentsFactory
import dev.johnoreilly.confetti.ui.components.NativeComponents
import dev.johnoreilly.confetti.ui.components.SduiArms
import dev.johnoreilly.confetti.ui.components.SduiComponents
import dev.johnoreilly.confetti.ui.components.SduiTracking
import dev.johnoreilly.confetti.ui.livery.LiveryAnalyticsEvent
import dev.johnoreilly.confetti.ui.livery.LiveryComponents
import dev.johnoreilly.confetti.ui.livery.LiveryStyling
import ee.schimke.livery.client.AnalyticsEventSink
import ee.schimke.livery.client.AnalyticsSink
import ee.schimke.livery.client.AndroidBundled
import ee.schimke.livery.client.Clock
import ee.schimke.livery.client.FileBlobStore
import ee.schimke.livery.client.FileKeyValueStore
import ee.schimke.livery.client.ImageResolver
import ee.schimke.livery.client.JavaCrypto
import ee.schimke.livery.client.KtorFetcher
import ee.schimke.livery.client.StaticSource
import ee.schimke.livery.client.TemplateRepository
import ee.schimke.livery.client.TrustPolicy
import ee.schimke.livery.client.liveryDefaults
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.OkHttpClient
import java.io.File
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * How the app finds this module: registered in META-INF/services, and present only in the
 * sduiDebug build type, so `ConferenceComponentsFactory.load()` returns it there and null in
 * every other variant.
 */
class LiveryComponentsFactory : ConferenceComponentsFactory {
    override fun create(
        context: Context,
        okHttp: OkHttpClient,
        analytics: AnalyticsLogger,
        options: Map<String, String>,
    ): ConferenceComponents = confettiComponents(
        context, okHttp, analytics,
        templatesUrl = options["templatesUrl"].orEmpty(),
        arms = FirebaseSduiArms(forced = options["forcedArm"]?.takeIf { it.isNotEmpty() }),
    )
}

/**
 * What the agenda, session and speaker screens draw with in the sduiDebug build: per conference,
 * the arm of the sdui experiment Firebase assigned (see [FirebaseSduiArms]). The Livery side,
 * repositories, bundled catalogs, refreshes and all, is built only when the first conference in
 * the livery arm draws, so a user whose conferences are all native never loads it.
 */
fun confettiComponents(
    context: Context,
    okHttp: OkHttpClient,
    analytics: AnalyticsLogger,
    /** Where each series' catalog is served (`<url>/<series>/manifest.json`); empty: bundled only. */
    templatesUrl: String = "",
    arms: SduiArms = FirebaseSduiArms(),
): SduiComponents {
    val tracking = SduiTracking(analytics)
    return SduiComponents(
        arms = arms,
        native = NativeComponents(tracking, SduiExperiment.NATIVE),
        livery = {
            LiveryComponents(
                styling = confettiLiveryStyling(context, liveryHttpClient(okHttp), analytics, templatesUrl),
                native = NativeComponents(tracking, SduiExperiment.LIVERY),
            )
        },
    )
}

/**
 * The arm per series from Firebase Remote Config: `sdui_<series>` (`sdui_droidcon`,
 * `sdui_kotlinconf`) holds `native` or `livery`. Run the split as a Firebase A/B Testing
 * experiment on those parameters: Firebase assigns users, counts exposures and reads the outcome
 * from Analytics, where every slot's events carry `exp_sdui`.
 *
 * `livery_enabled` is the kill switch: set it to `false` and every series draws
 * [SduiExperiment.NATIVE], so Livery is never built (no repository, no bundled catalog, no
 * player), whatever its arm. Use it when Livery is crashing or misbehaving in the field; the A/B
 * test's events then all report `exp_sdui=native`, so pause the experiment while it's off.
 *
 * Each value is read once per process and kept, so a background fetch never swaps the UI under
 * the user; the next launch draws what it activated (Remote Config activates a fetch on the next
 * read, and the app fetches at every launch). Until a fetch has activated, and wherever Firebase
 * isn't available (tests), Livery is enabled and a series draws [SduiExperiment.LIVERY].
 */
class FirebaseSduiArms(
    private val forced: String? = null,
    private val config: RemoteValues = FirebaseRemoteValues,
) : SduiArms {
    private val arms = ConcurrentHashMap<String, String>()

    /** Read once: a kill switch that flips mid-session applies from the next launch. */
    private val enabled: Boolean by lazy { config[KILL_SWITCH]?.toBooleanStrictOrNull() ?: true }

    override fun arm(series: String): String? {
        forced?.let { return it }
        if (!enabled) return SduiExperiment.NATIVE
        arms[series]?.let { return it }
        // Whatever this first lookup draws is kept, the default included: a value that arrives
        // later in this process (defaults or a fetch finishing) applies from the next launch.
        val value = config[key(series)]?.takeIf { it in SduiExperiment.ARMS } ?: SduiExperiment.LIVERY
        return arms.putIfAbsent(series, value) ?: value
    }

    /** Where the switch and arms come from: Firebase Remote Config in the app, a map in tests. */
    fun interface RemoteValues {
        /** The value of [key], or null when it has none. */
        operator fun get(key: String): String?
    }

    /** Firebase Remote Config, with every key's default, fetched and activated on first use. */
    object FirebaseRemoteValues : RemoteValues {
        private val remoteConfig by lazy {
            runCatching {
                Firebase.remoteConfig.apply {
                    setDefaultsAsync(
                        (ConferenceSeries.ALL + ConferenceSeries.BASE).associate { key(it) to SduiExperiment.LIVERY } +
                            (KILL_SWITCH to "true"),
                    )
                    fetchAndActivate()
                }
            }.getOrNull()
        }

        override fun get(key: String): String? =
            runCatching { remoteConfig?.getString(key) }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    companion object {
        /** Remote Config's kill switch: `false` turns Livery off everywhere. */
        const val KILL_SWITCH = "livery_enabled"

        private fun key(series: String) = "${SduiExperiment.ID}_$series"
    }
}

/**
 * Livery's Ktor client on the app's own [OkHttpClient] (its connection pool, cache, interceptors
 * and any proxy or pinning the app sets), with Livery's settings: no automatic redirects, since
 * Livery checks each hop itself, and its timeouts.
 */
fun liveryHttpClient(okHttp: OkHttpClient): HttpClient = HttpClient(OkHttp) {
    engine { preconfigured = okHttp }
    liveryDefaults()
}

/**
 * Livery on Android: one catalog per conference series, each
 *
 * - bundled into the APK's assets at `livery/<series>/` by the build (see build.gradle.kts), so it
 *   draws offline and on first launch;
 * - refreshed from `<templatesUrl>/<series>/` when the app gives one. Anything downloaded has a
 *   higher serial than the bundled copy and wins.
 *
 * The branch isn't signed yet, so this debug-only build accepts unsigned manifests (https from one
 * repository). A release build needs `TrustPolicy.Signed` with the public key of the CI's signing
 * key (`livery keygen confetti`, the private half as a CI secret, `--key` in the publish step).
 */
fun confettiLiveryStyling(
    context: Context,
    httpClient: HttpClient,
    analytics: AnalyticsLogger = AnalyticsLogger.None,
    templatesUrl: String = "",
    clock: Clock = Clock { System.currentTimeMillis() },
    refresh: Boolean = templatesUrl.isNotEmpty(),
): LiveryStyling {
    val app = context.applicationContext
    val fetcher = KtorFetcher(httpClient)
    // A catalog per series this build bundles (catalogs/<series>, built into :livery's assets),
    // the base one included: a series without one draws the base catalog's designs.
    val catalogs = (ConferenceSeries.ALL + ConferenceSeries.BASE)
        .filter { AndroidBundled.fromAssets(app, "livery/$it") != null }
        .associateWith { series ->
        TemplateRepository(
            source = StaticSource("${templatesUrl.trimEnd('/')}/$series/", id = "confetti-$series"),
            fetcher = fetcher,
            crypto = JavaCrypto,
            trust = if (templatesUrl.isNotEmpty()) TrustPolicy.AllowUnsigned else TrustPolicy.Signed(emptyList()),
            // Separate stores per catalog: one conference's cache never evicts another's.
            blobs = FileBlobStore(File(app.cacheDir, "livery/$series/blobs")),
            kv = FileKeyValueStore(File(app.filesDir, "livery/$series")),
            clock = clock,
            targets = listOf(LiveryStyling.InApp),
            bundled = AndroidBundled.fromAssets(app, "livery/$series"),
        )
    }
    return LiveryStyling(
        catalogs = catalogs,
        clock = clock,
        platform = "android",
        locale = Locale.getDefault().toLanguageTag(),
        appVersion = appVersion(app),
        bucketingId = bucketingId(app),
        events = AnalyticsEventSink(AnalyticsSink { name, params -> analytics.logEvent(LiveryAnalyticsEvent(name, params)) }),
        // Speaker photos come from the conference data's own hosts (sessionize.com redirects to
        // cdn.sessionize.com; subdomains match). A catalog image proxy would replace this list.
        images = { repository ->
            ImageResolver(
                repository, fetcher, JavaCrypto,
                directHosts = setOf("sessionize.com", "storage.googleapis.com", "githubusercontent.com"),
            )
        },
        refresh = refresh,
    )
}

@Suppress("DEPRECATION")
private fun appVersion(context: Context): Long =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode }.getOrDefault(0L)

/** A random id for sticky rollouts, scoped to this install: never the account. */
private fun bucketingId(context: Context): String {
    val file = File(context.filesDir, "livery/bucketing-id")
    return file.takeIf { it.isFile }?.readText()?.takeIf { it.isNotBlank() }
        ?: UUID.randomUUID().toString().also { file.parentFile?.mkdirs(); file.writeText(it) }
}
