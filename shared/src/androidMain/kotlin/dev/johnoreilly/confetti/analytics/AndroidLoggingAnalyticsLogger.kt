package dev.johnoreilly.confetti.analytics

import android.util.Log

object AndroidLoggingAnalyticsLogger: AnalyticsLogger {
    override fun logEvent(event: AnalyticsEvent) {
        Log.i("Analytics", "${event.id} ${event.properties}")
    }
}