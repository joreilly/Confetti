package dev.johnoreilly.confetti.wear.bookmarks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BookmarksRoute(
    component: BookmarksComponent,
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

    BookmarksScreen(
        uiState = uiState,
        sessionSelected = {
            component.onSessionClicked(it)
        },
    )
}