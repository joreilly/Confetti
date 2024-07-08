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
import com.apollographql.apollo.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.shared.R
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.nowInstant
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import kotlin.time.Duration.Companion.minutes

class SessionNotificationSender(
    private val context: Context,
    private val repository: ConfettiRepository,
    private val dateService: DateService,
    private val notificationManager: NotificationManagerCompat,
    private val authentication: Authentication,
) {

    suspend fun sendNotification() {
        // If there is no signed-in user, skip.
        val user = authentication.currentUser.value ?: return

        val conferenceId = repository.getConference()

        val sessionsResponse = repository.sessions(
            conference = conferenceId,
            uid = user.uid,
            tokenProvider = user,
            fetchPolicy = FetchPolicy.CacheFirst,
        )

        val sessions = sessionsResponse
            .data
            ?.sessions
            ?.nodes
            ?.map { query -> query.sessionDetails }
            .orEmpty()

        // If there are no available sessions, skip.
        if (sessions.isEmpty()) {
            return
        }

        // If current date is not in the conference range, skip.
        if (sessions.none { session -> session.startsAt.date == dateService.now().date }) {
            return
        }

        val bookmarks = repository.bookmarks(
            conference = conferenceId,
            uid = user.uid,
            tokenProvider = user,
            fetchPolicy = FetchPolicy.CacheFirst,
        ).first()
            .data
            ?.bookmarks
            ?.sessionIds
            .orEmpty()

        val bookmarkedSessions = sessions.filter { session ->
            bookmarks.contains(session.id)
        }

        val sessionsTimeZone = TimeZone.of(sessionsResponse.data?.config?.timezone.orEmpty())
        val timeZonedNow = dateService.nowInstant()
        val upcomingSessions = bookmarkedSessions.filter { session ->
            val timeZonedStartsAt = session.startsAt.toInstant(sessionsTimeZone)
            val untilInMinutes = timeZonedNow.until(timeZonedStartsAt, DateTimeUnit.MINUTE)
            untilInMinutes in 0..INTERVAL.inWholeMinutes
        }

        // If there are no bookmarked upcoming sessions, skip.
        if (upcomingSessions.isEmpty()) {
            return
        }

        createNotificationChannel()

        // If there are multiple notifications, we create a summary to group them.
        if (upcomingSessions.count() > 1) {
            sendNotification(SUMMARY_ID, createSummaryNotification(upcomingSessions))
        }

        // We reverse the sessions to show early sessions first.
        for ((id, session) in upcomingSessions.reversed().withIndex()) {
            sendNotification(id, createNotification(session))
        }
    }

    /**
     * Creates an interval from [DateService.now] up to [INTERVAL], with the device time zone.
     */
    private fun createIntervalRangeFromNow(): ClosedRange<LocalDateTime> {
        val timeZone = TimeZone.currentSystemDefault()
        val now = dateService.now()
        val future = (now.toInstant(timeZone) + INTERVAL).toLocalDateTime(timeZone)
        return now..future
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
            .setContentText("Starts at ${session.startsAt.time} in ${session.room?.name.orEmpty()}")
            .setGroup(GROUP)
            .setAutoCancel(true)
            .build()
    }

    private fun createSummaryNotification(sessions: List<SessionDetails>): Notification {
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
            .setStyle(style)
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
