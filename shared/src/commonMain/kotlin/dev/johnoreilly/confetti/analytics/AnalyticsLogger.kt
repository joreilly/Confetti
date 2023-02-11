package dev.johnoreilly.confetti.analytics

interface AnalyticsLogger {
    fun logEvent(event: AnalyticsEvent)

    object None: AnalyticsLogger {
        override fun logEvent(event: AnalyticsEvent) {
        }
    }
}