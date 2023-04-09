@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.tile

import androidx.wear.compose.material.Colors
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.toTimeZone
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import dev.johnoreilly.confetti.wear.settings.toMaterialThemeColors
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
        val theme = phoneSettingsSync.settingsFlow.first().theme
        val user = authentication.currentUser.value

        renderer.colors.value = theme?.toMaterialThemeColors() ?: Colors()

        val conference = phoneSettingsSync.conferenceFlow.first()

        if (conference.isBlank()) {
            return ConfettiTileData.NoConference
        }

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
        return renderer.renderTimeline(tileState(), requestParams)
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