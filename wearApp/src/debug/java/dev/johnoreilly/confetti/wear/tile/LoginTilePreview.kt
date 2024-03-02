package dev.johnoreilly.confetti.wear.tile

import android.content.Context
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import com.google.android.horologist.compose.tools.tileRendererPreviewData
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.CurrentSessionsData
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.NoConference
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.NotLoggedIn

@Preview
fun LoginTilePreview(context: Context): TilePreviewData {
    val tileState = NotLoggedIn(
        TestFixtures.kotlinConf2023Config
    )
    val renderer = CurrentSessionsTileRenderer(context)

    return tileRendererPreviewData(
        renderer, tileState, tileState
    )
}

@Preview
fun NoConferenceTilePreview(context: Context): TilePreviewData {

    val tileState = NoConference

    val renderer = CurrentSessionsTileRenderer(context)

    return tileRendererPreviewData(
        renderer, tileState, tileState
    )
}

@Preview
fun BookmarksTilePreview(context: Context): TilePreviewData {

    val tileState = CurrentSessionsData(
        TestFixtures.kotlinConf2023Config, listOf(
            TestFixtures.sessionDetails
        )
    )

    val renderer = CurrentSessionsTileRenderer(context)

    return tileRendererPreviewData(
        renderer, tileState, tileState
    )
}