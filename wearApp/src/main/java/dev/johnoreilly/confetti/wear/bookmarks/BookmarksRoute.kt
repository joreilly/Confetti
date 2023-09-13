package dev.johnoreilly.confetti.wear.bookmarks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@Composable
fun BookmarksRoute(
    component: BookmarksComponent,
    columnState: ScalingLazyColumnState,
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

    BookmarksScreen(
        uiState = uiState,
        sessionSelected = {
            component.onSessionClicked(it)
        },
        columnState = columnState
    )
}