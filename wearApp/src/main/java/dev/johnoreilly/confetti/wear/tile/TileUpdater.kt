package dev.johnoreilly.confetti.wear.tile

import androidx.wear.tiles.TileUpdateRequester

class TileUpdater(private val tileUpdateRequester: TileUpdateRequester) {
    fun updateBookmarksTile() {
        tileUpdateRequester
            .requestUpdate(CurrentSessionsTileService::class.java)
    }

    fun updateAll() {
        updateBookmarksTile()
    }
}