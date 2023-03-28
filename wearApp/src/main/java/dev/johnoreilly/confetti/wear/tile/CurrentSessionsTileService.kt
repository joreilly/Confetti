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
import dev.johnoreilly.confetti.toTimeZone
import dev.johnoreilly.confetti.wear.complication.nextSessionOrNull
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import dev.johnoreilly.confetti.wear.settings.toMaterialThemeColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class CurrentSessionsTileService : SuspendingTileService() {
    private val renderer = CurrentSessionsTileRenderer(this)

    private val repository: ConfettiRepository by inject()

    private val analyticsLogger: AnalyticsLogger by inject()

    private val phoneSettingsSync: PhoneSettingsSync by inject()

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return renderer.produceRequestedResources(tileState(), requestParams)
    }

    private suspend fun tileState(): CurrentSessionsData {
        val conference = repository.getConference()
        val data = repository.conferenceData(conference, FetchPolicy.CacheOnly).data
        val theme =
            phoneSettingsSync.settingsFlow.first().theme?.toMaterialThemeColors() ?: Colors()

        renderer.updateTheme(theme)

        val nextSession = data?.sessions?.nodes?.map { it.sessionDetails }
            ?.nextSessionOrNull(data.config.timezone.toTimeZone())

        return CurrentSessionsData(
            conference,
            nextSession?.startsAt,
            nextSession?.let { listOf(it) }.orEmpty()
        )
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