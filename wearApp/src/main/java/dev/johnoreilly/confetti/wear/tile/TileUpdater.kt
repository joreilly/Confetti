package dev.johnoreilly.confetti.wear.tile

import androidx.wear.tiles.TileUpdateRequester
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Asks the watch to redraw what shows the user's bookmarks: the tile, and in the sduiDebug build its widgets too. */
open class TileUpdater : KoinComponent {
    private val tileUpdateRequester: TileUpdateRequester by inject()

    fun updateBookmarksTile() {
        tileUpdateRequester
            .requestUpdate(CurrentSessionsTileService::class.java)
    }

    open fun updateAll() {
        updateBookmarksTile()
    }
}