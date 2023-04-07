package dev.johnoreilly.confetti.wear.bookmarks

import dev.johnoreilly.confetti.fragment.SessionDetails

sealed interface BookmarksUiState {
    object Error : BookmarksUiState
    object Loading : BookmarksUiState
    object NotLoggedIn : BookmarksUiState

    data class Success(
        val conference: String,
        val upcoming: List<SessionDetails>,
        val past: List<SessionDetails>
    ) : BookmarksUiState {
        val hasUpcomingBookmarks: Boolean
            get() = upcoming.isNotEmpty()
    }
}