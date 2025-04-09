package dev.johnoreilly.confetti.wear.bookmarks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPlaceholderState
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
    modifier: Modifier = Modifier,
) {
    val columnState = rememberTransformingLazyColumnState()

    val columnPadding = rememberResponsiveColumnPadding(
        first = ColumnItemType.ListHeader, last = ColumnItemType.Card
    )
    val placeholderState = rememberPlaceholderState { uiState !is QueryResult.Loading }
    ScreenScaffold(modifier = modifier, scrollState = columnState, contentPadding = columnPadding) { contentPadding ->
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = contentPadding,
        ) {
            when (uiState) {
                is QueryResult.Success, QueryResult.Loading -> {
                    item {
                        ScreenHeader(
                            text = stringResource(R.string.upcoming_sessions)
                        )
                    }

                    val upcoming = (uiState as? QueryResult.Success)?.result?.upcoming
                    val now = (uiState as? QueryResult.Success)?.result?.now
                    val itemCount = upcoming?.size ?: 2

                    items(itemCount) {
                        val session = upcoming?.get(it)
                        SessionCard(
                            session = session,
                            sessionSelected = {
                                sessionSelected(it)
                            },
                            currentTime = now,
                            isBookmarked = true,
                            addBookmark = addBookmark,
                            removeBookmark = removeBookmark,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if ((uiState as? QueryResult.Success)?.result?.hasUpcomingBookmarks == false) {
                        item {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(id = R.string.no_upcoming),
                            )
                        }
                    }

                    item {
                        SectionHeader(
                            modifier = Modifier.fillMaxWidth(), text = stringResource(id = R.string.past_sessions)
                        )
                    }

                    if (uiState is QueryResult.Success) {
                        items(uiState.result.past) { session ->
                            SessionCard(
                                session = session,
                                sessionSelected = {
                                    sessionSelected(it)
                                },
                                currentTime = uiState.result.now,
                                isBookmarked = true,
                                addBookmark = {},
                                removeBookmark = {},
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        if (uiState.result.past.isEmpty()) {
                            item {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(id = R.string.no_past),
                                )
                            }
                        }
                    }
                }

                is QueryResult.Error -> {
                    item {
                        ScreenHeader(
                            text = stringResource(R.string.upcoming_sessions)
                        )
                    }
                    item {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = uiState.exception.message ?: "Unknown Error Occurred",
                            color = MaterialTheme.colorScheme.errorDim
                        )
                    }
                }

                else -> {}
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
            ), sessionSelected = {}, addBookmark = {}, removeBookmark = {})
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun BookmarksPreviewLoading() {
    ConfettiThemeFixed {
        BookmarksScreen(uiState = QueryResult.Loading, sessionSelected = {}, addBookmark = {}, removeBookmark = {})
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun BookmarksPreviewError() {
    ConfettiThemeFixed {
        BookmarksScreen(
            uiState = QueryResult.Error(Exception("Boom")),
            sessionSelected = {},
            addBookmark = {},
            removeBookmark = {})
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun BookmarksPreviewErrorLong() {
    ConfettiThemeFixed {
        BookmarksScreen(
            uiState = QueryResult.Error(Exception("Boom: This is a long error message for testing purposes and to ensure it will be cropped correctly.")),
            sessionSelected = {},
            addBookmark = {},
            removeBookmark = {})
    }
}


@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun BookmarksPreviewEmpty() {
    ConfettiThemeFixed {
        BookmarksScreen(
            uiState = QueryResult.Success(
                BookmarksUiState(
                    conference = TestFixtures.kotlinConf2023.id,
                    upcoming = listOf(),
                    past = listOf(),
                    now = LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime()
                )
            ), sessionSelected = {}, addBookmark = {}, removeBookmark = {})
    }
}

