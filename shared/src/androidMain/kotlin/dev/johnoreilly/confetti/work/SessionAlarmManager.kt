package dev.johnoreilly.confetti.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Manage Android alarms for sessions.
 * Schedule inexact alarms 15 minutes before talk.
 * Inexact alarms save battery. Meet Android 12+ policies.
 */
class SessionAlarmManager(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(
        conferenceId: String,
        sessionId: String,
        sessionTitle: String,
        roomName: String,
        startsAtTime: String,
        triggerTimeMillis: Long
    ) {
        val intent = Intent(context, SessionAlarmReceiver::class.java).apply {
            putExtra(SessionAlarmReceiver.EXTRA_CONFERENCE_ID, conferenceId)
            putExtra(SessionAlarmReceiver.EXTRA_SESSION_ID, sessionId)
            putExtra(SessionAlarmReceiver.EXTRA_SESSION_TITLE, sessionTitle)
            putExtra(SessionAlarmReceiver.EXTRA_ROOM_NAME, roomName)
            putExtra(SessionAlarmReceiver.EXTRA_STARTS_AT_TIME, startsAtTime)
        }

        val requestCode = sessionId.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Schedule 15 minutes early. Inexact alarm drift can be
            // +/- 5 minutes. Alert still triggers before session starts.
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
            Log.d("SessionAlarmManager", "Scheduled alarm for $sessionId at $triggerTimeMillis")
        } catch (e: SecurityException) {
            Log.e("SessionAlarmManager", "Failed to schedule alarm due to security exception", e)
        }
    }

    fun cancelAlarm(sessionId: String) {
        val intent = Intent(context, SessionAlarmReceiver::class.java)
        val requestCode = sessionId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("SessionAlarmManager", "Cancelled alarm for $sessionId")
        }
    }
}
