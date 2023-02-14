package dev.johnoreilly.confetti.analytics

import androidx.navigation.NavBackStackEntry

object NavigationHelper {
    @Suppress("DEPRECATION")
    fun AnalyticsLogger.logNavigationEvent(navEntry: NavBackStackEntry) {
        if (this == AnalyticsLogger.None) return

        val arguments = navEntry.arguments
        val navArguments = navEntry.destination.arguments
        val loggingArguments: Map<String, String> = buildMap {
            navArguments.keys.forEach {
                val value = arguments?.get(it)

                if (value != null) {
                    put(it, value.toString())
                }
            }
        }

        logEvent(
            AnalyticsEvent.Navigation(
                navArguments["conference"] as? String,
                navEntry.destination.route ?: "none",
                loggingArguments
            )
        )
    }
}