package dev.johnoreilly.confetti

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.johnoreilly.confetti.work.SessionAlarmReceiver
import dev.johnoreilly.confetti.work.SessionAlarmManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class SessionNotificationTest {

    @Test
    fun testScheduleAndCancelAlarm() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)

        val sessionAlarmManager = SessionAlarmManager(context)

        val sessionId = "session_123"
        assertNull(shadowAlarmManager.getNextScheduledAlarm())

        val triggerTime = System.currentTimeMillis() + 100000
        sessionAlarmManager.scheduleAlarm(
            conferenceId = "kotlinconf2023",
            sessionId = sessionId,
            sessionTitle = "Kotlin Multiplatform State of Union",
            roomName = "Auditorium 1",
            startsAtTime = "10:00",
            triggerTimeMillis = triggerTime
        )

        val scheduledAlarm = requireNotNull(shadowAlarmManager.nextScheduledAlarm)
        assertEquals(triggerTime, scheduledAlarm.triggerAtTime)

        val pendingIntent = scheduledAlarm.operation
        assertNotNull(pendingIntent)
        val shadowPendingIntent = shadowOf(pendingIntent)
        val savedIntent = shadowPendingIntent.savedIntent
        assertNotNull(savedIntent)
        assertEquals("kotlinconf2023", savedIntent.getStringExtra(SessionAlarmReceiver.EXTRA_CONFERENCE_ID))
        assertEquals(sessionId, savedIntent.getStringExtra(SessionAlarmReceiver.EXTRA_SESSION_ID))
        assertEquals("Kotlin Multiplatform State of Union", savedIntent.getStringExtra(SessionAlarmReceiver.EXTRA_SESSION_TITLE))
        assertEquals("Auditorium 1", savedIntent.getStringExtra(SessionAlarmReceiver.EXTRA_ROOM_NAME))
        assertEquals("10:00", savedIntent.getStringExtra(SessionAlarmReceiver.EXTRA_STARTS_AT_TIME))

        sessionAlarmManager.cancelAlarm(sessionId)

        assertNull(shadowAlarmManager.getNextScheduledAlarm())
    }
}
