package dev.johnoreilly.confetti.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

object FirebaseAnalyticsLogger : AnalyticsLogger {
    val analytics by lazy { Firebase.analytics }

    override fun logEvent(event: AnalyticsEvent) {
        analytics.logEvent(mappedEvent(event)) {
            event.properties.forEach { (key, value) ->
                val mappedKey = mappedKey(key)
                if (value is Number) {
                    if (value is Float || value is Double) {
                        param(mappedKey, value.toDouble())
                    } else {
                        param(mappedKey, value.toLong())
                    }
                } else {
                    param(mappedKey, value.toString())
                }
            }
        }
    }

    private fun mappedKey(key: String): String =
        if (key == AnalyticsEvent.Navigation.Route) FirebaseAnalytics.Param.SCREEN_NAME else key

    private fun mappedEvent(event: AnalyticsEvent) =
        if (event.id == AnalyticsEvent.Navigation.EventId) FirebaseAnalytics.Event.SCREEN_VIEW else event.id
}