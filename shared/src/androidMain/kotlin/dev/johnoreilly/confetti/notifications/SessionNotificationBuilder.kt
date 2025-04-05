package dev.johnoreilly.confetti.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.shared.R
import dev.johnoreilly.confetti.work.SessionNotificationSender.Companion.CHANNEL_ID
import dev.johnoreilly.confetti.work.SessionNotificationSender.Companion.GROUP

class SessionNotificationBuilder(
    private val context: Context,
) {
    fun createNotification(session: SessionDetails, conferenceId: String, notificationId: Int): NotificationCompat.Builder {
        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.mipmap.ic_launcher_round
        )

        return NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(largeIcon)
            .setContentTitle(session.title)
            .setContentText("Starts at ${session.startsAt.time} in ${session.room?.name.orEmpty()}")
            .setGroup(GROUP)
            .setAutoCancel(false)
            .setLocalOnly(false)
            .setContentIntent(openSessionIntent(session, conferenceId))
            .addAction(unbookmarkAction(conferenceId, session.id, notificationId))
            .extend(
                NotificationCompat.WearableExtender()
                    .setBridgeTag("session:reminder")
            )
    }

    private fun openSessionIntent(session: SessionDetails, conferenceId: String): PendingIntent? {
        return PendingIntent.getActivity(
            context,
            0,
            Intent(Intent.ACTION_VIEW, "https://confetti-app.dev/conference/${conferenceId}/session/${session.id}".toUri()),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun unbookmarkAction(conferenceId: String, sessionId: String, notificationId: Int): NotificationCompat.Action {
        val unbookmarkIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, NotificationReceiver::class.java).apply {
                action = "REMOVE_BOOKMARK"
                putExtra("conferenceId", conferenceId)
                putExtra("sessionId", sessionId)
                putExtra("notificationId", notificationId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Action.Builder(null, "Remove Bookmark", unbookmarkIntent)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_ARCHIVE)
            .build()
    }

    fun createChannel(): NotificationChannelCompat.Builder {
        val name = "Upcoming sessions"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        return NotificationChannelCompat.Builder(CHANNEL_ID, importance)
            .setName(name)
            .setDescription("Session reminders for upcoming sessions")
            .setShowBadge(true)
    }
}