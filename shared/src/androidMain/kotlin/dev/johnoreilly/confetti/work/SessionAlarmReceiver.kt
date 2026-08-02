package dev.johnoreilly.confetti.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import dev.johnoreilly.confetti.notifications.SessionNotificationBuilder
import kotlin.random.Random

/**
 * BroadcastReceiver triggered by AlarmManager.
 * Show notification for upcoming bookmarked session.
 */
class SessionAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val conferenceId = intent.getStringExtra(EXTRA_CONFERENCE_ID) ?: return
        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return
        val sessionTitle = intent.getStringExtra(EXTRA_SESSION_TITLE) ?: return
        val roomName = intent.getStringExtra(EXTRA_ROOM_NAME).orEmpty()
        val startsAtTime = intent.getStringExtra(EXTRA_STARTS_AT_TIME).orEmpty()

        Log.d("SessionAlarmReceiver", "Received alarm for session: $sessionId")

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            Log.d("SessionAlarmReceiver", "Notifications are disabled. Skipping.")
            return
        }

        val notificationId = Random.nextInt(Integer.MAX_VALUE / 2, Integer.MAX_VALUE)
        val builder = SessionNotificationBuilder(context)
        
        notificationManager.createNotificationChannel(builder.createChannel().build())

        val notification = builder.createNotification(
            title = sessionTitle,
            roomName = roomName,
            startsAtTime = startsAtTime,
            sessionId = sessionId,
            conferenceId = conferenceId,
            notificationId = notificationId
        ).build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e("SessionAlarmReceiver", "Permission not granted to show notification", e)
        }
    }

    companion object {
        const val EXTRA_CONFERENCE_ID = "extra_conference_id"
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_SESSION_TITLE = "extra_session_title"
        const val EXTRA_ROOM_NAME = "extra_room_name"
        const val EXTRA_STARTS_AT_TIME = "extra_starts_at_time"
    }
}
