package dev.johnoreilly.confetti.wear.navigation

import dev.johnoreilly.confetti.analytics.AnalyticsEvent
import dev.johnoreilly.confetti.analytics.AnalyticsLogger

object NavigationHelper {
    fun AnalyticsLogger.logNavigationEvent(config: WearAppComponent.Config) {
        if (this == AnalyticsLogger.None) return

        logEvent(
            AnalyticsEvent.Navigation(
                config.loggingName,
                config.loggingArguments
            )
        )
    }
}