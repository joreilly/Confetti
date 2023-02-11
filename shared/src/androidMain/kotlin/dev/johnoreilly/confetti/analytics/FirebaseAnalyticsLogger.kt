package dev.johnoreilly.confetti.analytics

import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

object FirebaseAnalyticsLogger: AnalyticsLogger {
    val analytics by lazy { Firebase.analytics }

    override fun logEvent(event: AnalyticsEvent) {
        analytics.logEvent(event.id) {
            event.properties.forEach { (key, value) ->
                if (value is Number) {
                    if (value is Float || value is Double) {
                        param(key, value.toDouble())
                    } else {
                        param(key, value.toLong())
                    }
                } else {
                    param(key, value.toString())
                }
            }
        }
    }
}