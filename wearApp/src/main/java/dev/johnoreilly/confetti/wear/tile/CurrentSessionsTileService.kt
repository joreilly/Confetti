package dev.johnoreilly.confetti.wear.tile

import android.content.Intent
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.google.android.horologist.tiles.SuspendingTileService
import com.materialkolor.dynamicColorScheme
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.toTimeZone
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import dev.johnoreilly.confetti.wear.ui.toColor
import dev.johnoreilly.confetti.wear.ui.toWearMaterialColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.android.ext.android.inject
import java.time.Instant

class CurrentSessionsTileService : SuspendingTileService() {
    private val renderer = CurrentSessionsTileRenderer(this)

    private val repository: ConfettiRepository by inject()

    private val analyticsLogger: AnalyticsLogger by inject()

    private val phoneSettingsSync: PhoneSettingsSync by inject()

    private val authentication: Authentication by inject()

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return renderer.produceRequestedResources(tileState(), requestParams)
    }

    private suspend fun tileState(): ConfettiTileData {
        val user = authentication.currentUser.value

        val conference = phoneSettingsSync.conferenceFlow.first()

        if (conference.isBlank()) {
            return ConfettiTileData.NoConference
        }

        val seedColor = repository.getConferenceThemeColor().toColor()
        renderer.colors.value = dynamicColorScheme(seedColor = seedColor, isDark = true).toWearMaterialColors()

        val responseData = repository.bookmarkedSessionsQuery(
            conference, user?.uid, user, FetchPolicy.CacheOnly
        ).execute().data

        if (user == null) {
            return ConfettiTileData.NotLoggedIn(
                responseData?.config,
            )
        }

        return if (responseData != null) {
            val timeZone = responseData.config.timezone.toTimeZone()
            val now = Instant.now().toKotlinInstant().toLocalDateTime(timeZone)

            val bookmarks =
                responseData.bookmarkConnection?.nodes?.map { it.sessionDetails }?.filter {
                    it.startsAt > now
                }?.sortedBy { it.startsAt }.orEmpty()

            ConfettiTileData.CurrentSessionsData(responseData.config, bookmarks)
        } else {
            ConfettiTileData.NoConference
        }
    }

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val lastClickableId = requestParams.currentState.lastClickableId
        if (lastClickableId.isNotBlank()) {
            handleClick("confetti://confetti/$lastClickableId")
        }

        return renderer.renderTimeline(tileState(), requestParams)
    }

    private fun handleClick(uri: String) {
        TaskStackBuilder.create(this)
            .addNextIntentWithParentStack(
                Intent(
                    Intent.ACTION_VIEW,
                    uri.toUri()
                )
            )
            .startActivities()
    }

    override fun onTileAddEvent(requestParams: EventBuilders.TileAddEvent) {
        super.onTileAddEvent(requestParams)

        analyticsLogger.logEvent(TileAnalyticsEvent(TileAnalyticsEvent.Type.Add))
    }

    override fun onTileRemoveEvent(requestParams: EventBuilders.TileRemoveEvent) {
        super.onTileRemoveEvent(requestParams)

        analyticsLogger.logEvent(TileAnalyticsEvent(TileAnalyticsEvent.Type.Remove))
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)

        analyticsLogger.logEvent(TileAnalyticsEvent(TileAnalyticsEvent.Type.Enter, getConference()))
    }

    override fun onTileLeaveEvent(requestParams: EventBuilders.TileLeaveEvent) {
        super.onTileLeaveEvent(requestParams)

        analyticsLogger.logEvent(TileAnalyticsEvent(TileAnalyticsEvent.Type.Leave, getConference()))
    }

    private fun getConference(): String = runBlocking {
        // Not ideal, but runs on the Binder Thread
        repository.getConference()
    }
}