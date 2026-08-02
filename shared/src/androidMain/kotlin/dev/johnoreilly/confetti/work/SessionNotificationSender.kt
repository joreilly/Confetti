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
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.notifications.SessionNotificationBuilder
import dev.johnoreilly.confetti.notifications.SummaryNotificationBuilder
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.nowInstant
import dev.johnoreilly.confetti.work.NotificationSender.Selector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.toInstant
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

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
    private val alarmManager = SessionAlarmManager(context)
    private var bookmarksJob: Job? = null

    init {
        coroutineScope.launch {
            appSettings.notificationsEnabledFlow.collect { enabled ->
                if (enabled) {
                    startObservingBookmarks()
                } else {
                    stopObservingBookmarks()
                    cancelAllAlarms()
                }
            }
        }
    }

    private fun startObservingBookmarks() {
        bookmarksJob?.cancel()
        bookmarksJob = coroutineScope.launch {
            authentication.currentUser.collect { user ->
                if (user == null) {
                    cancelAllAlarms()
                    return@collect
                }

                val conferenceId = repository.getConference()
                if (conferenceId.isBlank()) return@collect

                repository.bookmarks(
                    conference = conferenceId,
                    uid = user.uid,
                    tokenProvider = user,
                    fetchPolicy = FetchPolicy.CacheFirst
                ).collect { response ->
                    val bookmarks = response.data?.bookmarks?.sessionIds.orEmpty().toSet()
                    rescheduleAlarms(conferenceId, user, bookmarks)
                }
            }
        }
    }

    private fun stopObservingBookmarks() {
        bookmarksJob?.cancel()
        bookmarksJob = null
    }

    private suspend fun rescheduleAlarms(
        conferenceId: String,
        user: User,
        bookmarks: Set<String>
    ) {
        val sessionsResponse = repository.sessions(
            conference = conferenceId,
            uid = user.uid,
            tokenProvider = user,
            fetchPolicy = FetchPolicy.CacheFirst
        )
        val timezoneString = sessionsResponse.data?.config?.timezone ?: "UTC"
        val conferenceTimeZone = kotlinx.datetime.TimeZone.of(timezoneString)

        val sessions = sessionsResponse.data?.sessions?.nodes
            ?.map { it.sessionDetails }
            .orEmpty()

        val now = dateService.now()
        val nowInstant = now.toInstant(conferenceTimeZone)

        for (session in sessions) {
            alarmManager.cancelAlarm(session.id)
        }

        val upcomingBookmarkedSessions = sessions.filter { session ->
            bookmarks.contains(session.id) && session.startsAt.toInstant(conferenceTimeZone) > nowInstant
        }

        for (session in upcomingBookmarkedSessions) {
            val sessionInstant = session.startsAt.toInstant(conferenceTimeZone)
            val triggerInstant = sessionInstant.minus(15.minutes)
            val triggerTimeMillis = triggerInstant.toEpochMilliseconds()

            alarmManager.scheduleAlarm(
                conferenceId = conferenceId,
                sessionId = session.id,
                sessionTitle = session.title,
                roomName = session.room?.name.orEmpty(),
                startsAtTime = session.startsAt.time.toString(),
                triggerTimeMillis = triggerTimeMillis
            )
        }
    }

    private suspend fun cancelAllAlarms() {
        val conferenceId = repository.getConference()
        if (conferenceId.isBlank()) return

        val sessionsResponse = repository.sessions(
            conference = conferenceId,
            uid = null,
            tokenProvider = null,
            fetchPolicy = FetchPolicy.CacheFirst
        )
        val sessions = sessionsResponse.data?.sessions?.nodes
            ?.map { it.sessionDetails }
            .orEmpty()

        for (session in sessions) {
            alarmManager.cancelAlarm(session.id)
        }
    }

    override suspend fun sendNotification(selector: Selector) {
        val notificationsEnabled = notificationManager.areNotificationsEnabled()

        if (!notificationsEnabled) {
            return
        }

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

        if (upcomingSessions.isEmpty()) {
            return
        }

        createNotificationChannel()

        if (upcomingSessions.count() > 1) {
            sendNotification(
                SUMMARY_ID,
                summaryNotificationBuilder.createSummaryNotification(upcomingSessions, SUMMARY_ID).build()
            )
        }

        for (session in upcomingSessions.reversed()) {
            val notificationId = Random.nextInt(Integer.MAX_VALUE / 2, Integer.MAX_VALUE)
            sendNotification(
                notificationId,
                sessionNotificationBuilder.createNotification(session, conferenceId, notificationId).build()
            )
        }
    }

    private fun createNotificationChannel() {
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
        coroutineScope.launch {
            if (enabled) {
                startObservingBookmarks()
                updateSchedule()
            } else {
                stopObservingBookmarks()
                cancelAllAlarms()
            }
        }
    }

    companion object {
        internal val CHANNEL_ID = "SessionNotification"
        internal val GROUP = "dev.johnoreilly.confetti.SESSIONS_ALERT"
        private val SUMMARY_ID = 10
    }
}
