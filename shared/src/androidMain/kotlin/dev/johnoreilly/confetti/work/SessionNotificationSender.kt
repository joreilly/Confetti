package dev.johnoreilly.confetti.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.shared.R
import dev.johnoreilly.confetti.utils.DateService
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

class SessionNotificationSender(
    private val context: Context,
    private val repository: ConfettiRepository,
    private val dateService: DateService,
    private val notificationManager: NotificationManagerCompat,
    private val authentication: Authentication,
) {

    suspend fun sendNotification() {
        Log.d("marcello", "notify")

        // If there is no user signed-in, no reason to process all sessions.
        val user = authentication.currentUser.value ?: return

        val conferenceId = repository.getConference()
        val conferenceDates = repository.conferenceData(conferenceId, FetchPolicy.CacheAndNetwork)
            .data
            ?.sessions
            ?.nodes
            ?.map { session -> session.sessionDetails.startsAt.date }
            .orEmpty()

        // If currente date is not in the conference range, no reason to process all sessions.
        if (dateService.now().date !in conferenceDates) {
            return
        }

        val bookmarks = repository.bookmarks(
            conference = conferenceId,
            uid = user.uid,
            tokenProvider = user,
            fetchPolicy = FetchPolicy.CacheAndNetwork,
        )
            .data
            ?.bookmarks
            ?.sessionIds
            .orEmpty()

        val sessions = repository.sessions(
            conference = conferenceId,
            uid = user.uid,
            tokenProvider = user,
            fetchPolicy = FetchPolicy.CacheAndNetwork,
        )
            .data
            ?.sessions
            ?.nodes
            ?.map { it.sessionDetails }
            .orEmpty()

        val bookmarkedSessions = sessions.filter { session ->
            bookmarks.contains(session.id)
        }

        val intervalRange = createIntervalRange()
        val sessionsToNotify = bookmarkedSessions.filter { session ->
            session.startsAt in intervalRange
        }

        if (sessionsToNotify.isNotEmpty()) {
            createNotificationChannel()
            sendNotification(SUMMARY_ID, createSummaryNotification(sessionsToNotify.count()))
            for ((id, session) in sessionsToNotify.reversed().withIndex()) {
                sendNotification(id, createNotification(session))
            }
        }
    }

    private fun createIntervalRange(): ClosedRange<LocalDateTime> {
        val now = dateService.now()
        val timeZone = TimeZone.currentSystemDefault()
        val past = now.toInstant(timeZone) - INTERVAL
        return past.toLocalDateTime(timeZone)..now
    }

    private fun createNotificationChannel() {
        // Channels are only available on Android O+.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val name = "Upcoming sessions"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = ""
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(session: SessionDetails): Notification {
        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.mipmap.ic_launcher_round
        )

        return NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(largeIcon)
            .setContentTitle(session.title)
            .setContentText("Room: ${session.room?.name.orEmpty()}")
            .setGroup(GROUP)
            .setAutoCancel(true)
            .build()
    }

    private fun createSummaryNotification(upcomingSessionsCount: Int): Notification {
        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.mipmap.ic_launcher_round
        )

        return NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(largeIcon)
            .setContentTitle("$upcomingSessionsCount upcoming sessions")
            .setGroup(GROUP)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
    }

    private fun sendNotification(id: Int, notification: Notification) {
        try {
            notificationManager.notify(id, notification)
        } catch (e: SecurityException) {
            Log.e("SessionNotification", "Permission for notification has not been granted.", e)
        }
    }

    companion object {
        private val CHANNEL_ID = "SessionNotification"
        private val GROUP = "dev.johnoreilly.confetti.SESSIONS_ALERT"
        private val SUMMARY_ID = 0

        // Minimum interval for work manager: MIN_PERIODIC_INTERVAL_MILLIS
        val INTERVAL = 15.minutes
    }
}
