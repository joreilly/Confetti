@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.bookmarks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import org.koin.androidx.compose.getViewModel

@Composable
fun BookmarksRoute(
    navigateToSession: (SessionDetailsKey) -> Unit,
    columnState: ScalingLazyColumnState,
) {
    val viewModel: BookmarksViewModel = getViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BookmarksScreen(
        uiState = uiState,
        sessionSelected = {
            navigateToSession(it)
        },
        columnState = columnState
    )
}

@Composable
fun BookmarksScreen(
    uiState: BookmarksUiState,
    sessionSelected: (SessionDetailsKey) -> Unit,
    columnState: ScalingLazyColumnState
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        when (uiState) {
            is BookmarksUiState.Success -> {
                item { SectionHeader(text = "Upcoming Sessions") }

                items(uiState.upcoming) { session ->
                    SessionCard(session) {
                        sessionSelected(
                            SessionDetailsKey(
                                conference = uiState.conference,
                                sessionId = it
                            )
                        )
                    }
                }

                if (uiState.upcoming.isEmpty()) {
                    item {
                        Text("No upcoming sessions")
                    }
                }

                item { SectionHeader(text = "Past Sessions") }

                items(uiState.past) { session ->
                    SessionCard(session) {
                        sessionSelected(
                            SessionDetailsKey(
                                conference = uiState.conference,
                                sessionId = it
                            )
                        )
                    }
                }

                if (uiState.past.isEmpty()) {
                    item {
                        Text("No past sessions")
                    }
                }
            }

            else -> {
                // TODO
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun BookmarksPreview() {
    ConfettiTheme {
        BookmarksScreen(
            uiState = BookmarksUiState.Success(
                conference = TestFixtures.kotlinConf2023.id,
                upcoming = listOf(TestFixtures.sessionDetails),
                past = listOf(),
            ),
            sessionSelected = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}
