@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.bookmarks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.toKotlinLocalDateTime
import org.koin.androidx.compose.getViewModel
import java.time.LocalDateTime

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
    uiState: QueryResult<BookmarksUiState>,
    sessionSelected: (SessionDetailsKey) -> Unit,
    columnState: ScalingLazyColumnState
) {
    val now = remember { LocalDateTime.now().toKotlinLocalDateTime() }
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        when (uiState) {
            is QueryResult.Success -> {
                item { SectionHeader(text = "Upcoming Sessions") }

                items(uiState.result.upcoming) { session ->
                    SessionCard(
                        session = session,
                        sessionSelected = {
                            sessionSelected(
                                SessionDetailsKey(
                                    conference = uiState.result.conference,
                                    sessionId = it
                                )
                            )
                        }, currentTime = now
                    )
                }

                if (!uiState.result.hasUpcomingBookmarks) {
                    item {
                        Text("No upcoming sessions")
                    }
                }

                item { SectionHeader(text = "Past Sessions") }

                items(uiState.result.past) { session ->
                    SessionCard(
                        session = session,
                        sessionSelected = {
                            sessionSelected(
                                SessionDetailsKey(
                                    conference = uiState.result.conference,
                                    sessionId = it
                                )
                            )
                        }, currentTime = now
                    )
                }

                if (uiState.result.past.isEmpty()) {
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
    ConfettiThemeFixed {
        BookmarksScreen(
            uiState = QueryResult.Success(
                BookmarksUiState(
                    conference = TestFixtures.kotlinConf2023.id,
                    upcoming = listOf(TestFixtures.sessionDetails),
                    past = listOf(),
                )
            ),
            sessionSelected = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}
