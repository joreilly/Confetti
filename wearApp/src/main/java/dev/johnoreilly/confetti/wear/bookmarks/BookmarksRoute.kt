package dev.johnoreilly.confetti.wear.bookmarks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState

@Composable
fun BookmarksRoute(
    component: BookmarksComponent,
    columnState: ScalingLazyColumnState = rememberColumnState(),
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(scrollState = columnState) {
        BookmarksScreen(
            uiState = uiState,
            sessionSelected = {
                component.onSessionClicked(it)
            },
            columnState = columnState
        )
    }
}