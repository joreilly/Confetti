package dev.johnoreilly.confetti.work

import android.app.Notification
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.apollographql.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.notifications.SessionNotificationBuilder
import dev.johnoreilly.confetti.notifications.SummaryNotificationBuilder
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.work.NotificationSender.Selector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

class SessionNotificationSender(
    private val context: Context,
    private val repository: ConfettiRepository,
    private val dateService: DateService,
    private val notificationManager: NotificationManagerCompat,
    private val authentication: Authentication,
    private val appSettings: AppSettings,
    private val coroutineScope: CoroutineScope,
    private val workManager: WorkManager,
) : NotificationSender {
    private val sessionNotificationBuilder = SessionNotificationBuilder(context)
    private val summaryNotificationBuilder = SummaryNotificationBuilder(context)

    override suspend fun sendNotification(selector: Selector) {
        val notificationsEnabled = notificationManager.areNotificationsEnabled()

        if (!notificationsEnabled) {
            return
        }

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

        val now = dateService.now()
        val upcomingSessions = bookmarkedSessions.filter { session ->
            selector.matches(now, session)
        }

        // If there are no bookmarked upcoming sessions, skip.
        if (upcomingSessions.isEmpty()) {
            return
        }

        createNotificationChannel()

        // If there are multiple notifications, we create a summary to group them.
        if (upcomingSessions.count() > 1) {
            sendNotification(
                SUMMARY_ID,
                summaryNotificationBuilder.createSummaryNotification(upcomingSessions, SUMMARY_ID).build()
            )
        }

        // We reverse the sessions to show early sessions first.
        for (session in upcomingSessions.reversed()) {
            val notificationId = Random.nextInt(Integer.MAX_VALUE / 2, Integer.MAX_VALUE)
            sendNotification(
                notificationId,
                sessionNotificationBuilder.createNotification(session, conferenceId, notificationId).build()
            )
        }
    }

    private fun createNotificationChannel() {
        // Channels are only available on Android O+.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        notificationManager.createNotificationChannel(sessionNotificationBuilder.createChannel().build())
    }

    private fun sendNotification(id: Int, notification: Notification) {
        try {
            notificationManager.notify(id, notification)
        } catch (e: SecurityException) {
            Log.e("SessionNotification", "Permission for notification has not been granted.", e)
        }
    }

    override suspend fun updateSchedule() {
        updateSchedule(appSettings.notificationsEnabledFlow.first())
    }

    override fun updateSchedule(enabled: Boolean) {
        if (enabled) {
            SessionNotificationWorker.startPeriodicWorkRequest(workManager)
        } else {
            SessionNotificationWorker.cancelWorkRequest(workManager)
        }
    }

    companion object {
        internal val CHANNEL_ID = "SessionNotification"
        internal val GROUP = "dev.johnoreilly.confetti.SESSIONS_ALERT"
        private val SUMMARY_ID = 10
    }
}
