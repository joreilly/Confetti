package dev.johnoreilly.confetti.wear.complication

import android.app.PendingIntent
import dev.johnoreilly.confetti.fragment.SessionDetails

data class NextSessionComplicationData(
    val sessionDetails: SessionDetails?,
    val launchIntent: PendingIntent?
)