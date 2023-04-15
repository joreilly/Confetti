package dev.johnoreilly.confetti.wear.bookmarks

import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.datetime.LocalDateTime

data class BookmarksUiState(
    val conference: String,
    val upcoming: List<SessionDetails>,
    val past: List<SessionDetails>,
    val now: LocalDateTime
) {
    val hasUpcomingBookmarks: Boolean
        get() = upcoming.isNotEmpty()
}
