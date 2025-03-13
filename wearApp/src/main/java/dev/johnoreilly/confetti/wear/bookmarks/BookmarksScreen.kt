package dev.johnoreilly.confetti.wear.bookmarks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.components.ScreenHeader
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime


@Composable
fun BookmarksScreen(
    uiState: QueryResult<BookmarksUiState>,
    sessionSelected: (String) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
) {
    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = rememberResponsiveColumnPadding(
                first = ColumnItemType.ListHeader,
                last = ColumnItemType.Card
            ),
        ) {
            when (uiState) {
                is QueryResult.Success -> {
                    item {
                        ScreenHeader(
                            text = stringResource(R.string.upcoming_sessions)
                        )
                    }

                    items(uiState.result.upcoming) { session ->
                        SessionCard(
                            modifier = Modifier
                                .fillMaxWidth(),
                            session = session,
                            sessionSelected = {
                                sessionSelected(it)
                            },
                            currentTime = uiState.result.now,
                            isBookmarked = true,
                            addBookmark = addBookmark,
                            removeBookmark = removeBookmark
                        )
                    }

                    if (!uiState.result.hasUpcomingBookmarks) {
                        item {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                text = stringResource(id = R.string.no_upcoming),
                            )
                        }
                    }

                    item {
                        SectionHeader(
                            modifier = Modifier
                                .fillMaxWidth(),
                            text = stringResource(id = R.string.past_sessions)
                        )
                    }

                    items(uiState.result.past) { session ->
                        SessionCard(
                            modifier = Modifier
                                .fillMaxWidth(),
                            session = session,
                            sessionSelected = {
                                sessionSelected(it)
                            },
                            currentTime = uiState.result.now,
                            isBookmarked = true,
                            addBookmark = {},
                            removeBookmark = {}
                        )
                    }

                    if (uiState.result.past.isEmpty()) {
                        item {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                text = stringResource(id = R.string.no_past),
                            )
                        }
                    }
                }

                else -> {
                    // TODO
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun BookmarksPreview() {
    ConfettiThemeFixed {
        BookmarksScreen(
            uiState = QueryResult.Success(
                BookmarksUiState(
                    conference = TestFixtures.kotlinConf2023.id,
                    upcoming = listOf(TestFixtures.sessionDetails),
                    past = listOf(),
                    now = LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime()
                )
            ),
            sessionSelected = {},
            addBookmark = {},
            removeBookmark = {}
        )
    }
}
