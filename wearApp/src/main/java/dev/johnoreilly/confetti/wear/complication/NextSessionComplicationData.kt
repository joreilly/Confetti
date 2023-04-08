package dev.johnoreilly.confetti.wear.complication

import android.app.PendingIntent
import dev.johnoreilly.confetti.GetBookmarkedSessionsQuery
import dev.johnoreilly.confetti.fragment.SessionDetails

data class NextSessionComplicationData(
    val sessionDetails: SessionDetails? = null,
    val conference: GetBookmarkedSessionsQuery.Config? = null,
    val launchIntent: PendingIntent?
)