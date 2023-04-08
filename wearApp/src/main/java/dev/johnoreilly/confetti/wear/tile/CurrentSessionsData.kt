package dev.johnoreilly.confetti.wear.tile

import dev.johnoreilly.confetti.GetBookmarkedSessionsQuery
import dev.johnoreilly.confetti.fragment.SessionDetails

sealed interface ConfettiTileData {

    data class CurrentSessionsData(
        val conference: GetBookmarkedSessionsQuery.Config,
        val bookmarks: List<SessionDetails>
    ): ConfettiTileData

    data class NotLoggedIn(
        val conference: GetBookmarkedSessionsQuery.Config?,
    ): ConfettiTileData

    object NoConference: ConfettiTileData
}