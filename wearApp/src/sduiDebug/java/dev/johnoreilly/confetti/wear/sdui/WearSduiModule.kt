package dev.johnoreilly.confetti.wear.sdui

import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.livery.BuildConfig
import dev.johnoreilly.confetti.livery.FirebaseSduiArms
import dev.johnoreilly.confetti.livery.confettiLiveryStyling
import dev.johnoreilly.confetti.livery.liveryHttpClient
import dev.johnoreilly.confetti.wear.components.NativeWearComponents
import dev.johnoreilly.confetti.wear.components.WearComponents
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * The Wear sdui build's Koin module, added after the app's own: it overrides the app's
 * `WearComponents` (Confetti's own composables) with the sdui experiment's, per conference series
 * as on the phone (Firebase's `sdui_<series>` arm). The Livery side is built on first use, so a
 * watch whose conferences are all in the native arm never builds it.
 */
val wearSduiModule = module {
    single<WearComponents> {
        val context = androidContext()
        val okHttp = get<OkHttpClient>()
        val analytics = get<AnalyticsLogger>()
        WearSduiComponents(
            arms = FirebaseSduiArms(forced = BuildConfig.FORCED_ARM.ifEmpty { null }),
            native = NativeWearComponents,
        ) {
            WearLiveryComponents(
                confettiLiveryStyling(context, liveryHttpClient(okHttp), analytics, templatesUrl = BuildConfig.TEMPLATES_URL),
                native = NativeWearComponents,
            )
        }
    }
}
