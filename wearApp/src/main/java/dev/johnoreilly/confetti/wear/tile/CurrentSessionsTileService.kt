@file:OptIn(ExperimentalHorologistTilesApi::class)

package dev.johnoreilly.confetti.wear.tile

import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.tiles.ExperimentalHorologistTilesApi
import com.google.android.horologist.tiles.SuspendingTileService
import dev.johnoreilly.confetti.ConfettiRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import org.koin.android.ext.android.inject
import java.time.Instant
import java.time.LocalDate

class CurrentSessionsTileService : SuspendingTileService() {
    private val renderer = CurrentSessionsTileRenderer(this)

    private val repository: ConfettiRepository by inject()

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources {
        return renderer.produceRequestedResources(tileState(), requestParams)
    }

    private suspend fun tileState(): CurrentSessionsData {
        val today = LocalDate.now().toKotlinLocalDate()
        val todaysSessions = repository.sessionsMap.first()[today].orEmpty()

        val now = Instant.now().toKotlinInstant()

        val nextSessionTime = todaysSessions.map { it.startInstant }
            .filter { it > now }
            .minOrNull()

        return CurrentSessionsData(
            nextSessionTime?.toLocalDateTime(TimeZone.currentSystemDefault()),
            todaysSessions.filter { it.startInstant == nextSessionTime }
        )
    }

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        return renderer.renderTimeline(tileState(), requestParams)
    }
}