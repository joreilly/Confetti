package dev.johnoreilly.confetti.sdui

import dev.johnoreilly.confetti.livery.BuildConfig
import dev.johnoreilly.confetti.livery.FirebaseSduiArms
import dev.johnoreilly.confetti.livery.confettiComponents
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * The sdui build's Koin module, added after the app's own: it overrides shared's
 * `ConferenceComponents` binding (Confetti's own composables) with the sdui experiment's, built
 * from the app's Context, OkHttpClient and AnalyticsLogger. Its settings are this module's own
 * (BuildConfig), not the app's.
 */
val sduiModule = module {
    single<ConferenceComponents> {
        confettiComponents(
            context = androidContext(),
            okHttp = get(),
            analytics = get(),
            arms = FirebaseSduiArms(forced = BuildConfig.FORCED_ARM.ifEmpty { null }),
        )
    }
}
