package dev.johnoreilly.confetti.analytics

interface AnalyticsEvent {
    val id: String
    val properties: Map<String, Any>

    data class Navigation(val conference: String, val route: String?, val arguments: Map<String, String>): AnalyticsEvent {
        override val id: String = "navigation"

        override val properties: Map<String, Any> = buildMap {
            putAll(arguments)

            put("route", (route ?: "none"))
            put("conference", conference)
        }
    }
}