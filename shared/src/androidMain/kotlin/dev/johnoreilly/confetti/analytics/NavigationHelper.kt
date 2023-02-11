package dev.johnoreilly.confetti.analytics

import androidx.navigation.NavBackStackEntry

object NavigationHelper {
    @Suppress("DEPRECATION")
    fun AnalyticsLogger.logNavigationEvent(conference: String, navEntry: NavBackStackEntry) {
        if (this == AnalyticsLogger.None) return

        val arguments = navEntry.arguments
        val loggingArguments: Map<String, String> = buildMap {
            navEntry.destination.arguments.keys.forEach {
                val value = arguments?.get(it)

                if (value != null) {
                    put(it, value.toString())
                }
            }
        }

        logEvent(
            AnalyticsEvent.Navigation(
                conference,
                navEntry.destination.route ?: "none",
                loggingArguments
            )
        )
    }
}