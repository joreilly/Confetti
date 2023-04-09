package dev.johnoreilly.confetti.wear.bookmarks

import dev.johnoreilly.confetti.fragment.SessionDetails

data class BookmarksUiState(
    val conference: String,
    val upcoming: List<SessionDetails>,
    val past: List<SessionDetails>
) {
    val hasUpcomingBookmarks: Boolean
        get() = upcoming.isNotEmpty()
}
