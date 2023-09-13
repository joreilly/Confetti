package dev.johnoreilly.confetti.wear.tile

import androidx.wear.tiles.TileUpdateRequester
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TileUpdater : KoinComponent {
    private val tileUpdateRequester: TileUpdateRequester by inject()

    fun updateBookmarksTile() {
        tileUpdateRequester
            .requestUpdate(CurrentSessionsTileService::class.java)
    }

    fun updateAll() {
        updateBookmarksTile()
    }
}