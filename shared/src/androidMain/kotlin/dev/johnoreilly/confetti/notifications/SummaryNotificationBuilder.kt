package dev.johnoreilly.confetti.notifications

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.shared.R
import dev.johnoreilly.confetti.work.SessionNotificationSender.Companion.CHANNEL_ID
import dev.johnoreilly.confetti.work.SessionNotificationSender.Companion.GROUP

class SummaryNotificationBuilder(
    private val context: Context,
) {

    fun createSummaryNotification(sessions: List<SessionDetails>, notificationId: Int): NotificationCompat.Builder {
        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.mipmap.ic_launcher_round
        )

        // Apply scope function is failing with an error:
        // InboxStyle.apply can only be called from within the same library group prefix.
        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle("${sessions.count()} upcoming sessions")

        // We only show up to a limited number of sessions to avoid pollute the user notifications.
        for (session in sessions.take(4)) {
            style.addLine(session.title)
        }

        return NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(largeIcon)
            .setGroup(GROUP)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setLocalOnly(false)
            .setStyle(style)
            .extend(NotificationCompat.WearableExtender().setBridgeTag("session:summary"))
    }
}