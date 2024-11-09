package dev.johnoreilly.confetti.wear.bookmarks

import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.datetime.LocalDateTime

data class BookmarksUiState(
    val conference: String,
    val upcoming: List<SessionDetails>,
    val past: List<SessionDetails>,
    val now: LocalDateTime
) {
    fun isBookmarked(id: String): Boolean {
        return (upcoming.find { it.id == id } != null) || (past.find { it.id == id } != null)
    }

    val hasUpcomingBookmarks: Boolean
        get() = upcoming.isNotEmpty()
}

