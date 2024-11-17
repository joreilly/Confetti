package dev.johnoreilly.confetti.wear.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.scrollTransform
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.rememberPlaceholderState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.components.PlaceholderButton
import dev.johnoreilly.confetti.wear.components.ScreenHeader
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    uiState: QueryResult<HomeUiState>,
    bookmarksUiState: QueryResult<BookmarksUiState>,
    sessionSelected: (String) -> Unit,
    daySelected: (LocalDate) -> Unit,
    onSettingsClick: () -> Unit,
    addBookmark: ((sessionId: String) -> Unit)?,
    removeBookmark: ((sessionId: String) -> Unit)?,
    onBookmarksClick: () -> Unit,
) {
    val dayFormatter = remember { DateTimeFormatter.ofPattern("cccc") }

    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = rememberResponsiveColumnPadding(
                first = ColumnItemType.ListHeader,
                last = ColumnItemType.IconButton,
            ),
        ) {
            titleSection(uiState = uiState)

            bookmarksSection(
                uiState = uiState,
                bookmarksUiState = bookmarksUiState,
                sessionSelected = sessionSelected,
                addBookmark = addBookmark,
                removeBookmark = removeBookmark,
                onBookmarksClick = onBookmarksClick
            )

            conferenceDaysSection(uiState = uiState, daySelected = daySelected, dayFormatter = dayFormatter)

            bottomMenuSection(onSettingsClick = onSettingsClick)
        }
    }
}

private fun TransformingLazyColumnScope.titleSection(uiState: QueryResult<HomeUiState>) {
    when (uiState) {
        is QueryResult.Success -> {
            item {
                ScreenHeader(
                    modifier = Modifier
                        .scrollTransform(this@item), text = uiState.result.conferenceName
                )
            }
        }

        QueryResult.Loading -> {
            item {
                val chipPlaceholderState = rememberPlaceholderState { false }
                ScreenHeader(
                    "",
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .placeholder(chipPlaceholderState)
                        .scrollTransform(this@item)
                )
            }
        }

        else -> {}
    }
}

private fun TransformingLazyColumnScope.bookmarksSection(
    uiState: QueryResult<HomeUiState>,
    bookmarksUiState: QueryResult<BookmarksUiState>,
    sessionSelected: (String) -> Unit,
    addBookmark: ((sessionId: String) -> Unit)?,
    removeBookmark: ((sessionId: String) -> Unit)?,
    onBookmarksClick: () -> Unit
) {
    item {
        SectionHeader(
            modifier = Modifier
                .fillMaxWidth()
                .scrollTransform(this@item),
            text = stringResource(R.string.home_bookmarked_sessions)
        )
    }

    when (bookmarksUiState) {
        is QueryResult.Success -> {
            val upcoming = bookmarksUiState.result.upcoming
            items(upcoming.take(3)) { session ->
                key(session.id) {
                    SessionCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scrollTransform(this@items),
                        session = session, sessionSelected = {
                            if (uiState is QueryResult.Success) {
                                sessionSelected(it)
                            }
                        },
                        currentTime = bookmarksUiState.result.now,
                        addBookmark = addBookmark,
                        removeBookmark = removeBookmark,
                        isBookmarked = bookmarksUiState.result.isBookmarked(session.id)
                    )
                }
            }
            if (upcoming.isEmpty()) {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scrollTransform(this@item),
                        text = stringResource(id = R.string.no_upcoming),
                    )
                }
            }
        }

        else -> {}
    }

    item {
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .scrollTransform(this@item),
            onClick = {
                if (uiState is QueryResult.Success) {
                    onBookmarksClick()
                }
            }
        ) {
            Text(stringResource(id = R.string.all_bookmarks))
        }
    }
}

private fun TransformingLazyColumnScope.conferenceDaysSection(
    uiState: QueryResult<HomeUiState>,
    daySelected: (LocalDate) -> Unit,
    dayFormatter: DateTimeFormatter
) {
    item {
        SectionHeader(
            modifier = Modifier
                .fillMaxWidth()
                .scrollTransform(this@item),
            text = stringResource(id = R.string.conference_days)
        )
    }
    when (uiState) {
        is QueryResult.Success -> {
            items(uiState.result.confDates) { date ->
                DayChip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scrollTransform(this@items),
                    dayFormatter,
                    date,
                    daySelected = { daySelected(date) })
            }
        }

        QueryResult.Loading -> {
            items(2) {
                PlaceholderButton()
            }
        }

        else -> {}
    }
}

@Composable
fun DayChip(
    modifier: Modifier = Modifier,
    dayFormatter: DateTimeFormatter,
    date: LocalDate,
    daySelected: () -> Unit
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = daySelected,
        colors = ButtonDefaults.filledVariantButtonColors(),
    ) {
        Text(dayFormatter.format(date.toJavaLocalDate()))
    }
}

private fun TransformingLazyColumnScope.bottomMenuSection(onSettingsClick: () -> Unit) {
    item {
        IconButton(
            modifier = Modifier
                .fillMaxWidth()
                .scrollTransform(this@item),
            onClick = onSettingsClick
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.home_settings_content_description),
            )
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun HomeListViewPreview() {
    ConfettiThemeFixed {
        HomeScreen(
            uiState = QueryResult.Success(
                HomeUiState(
                    conference = TestFixtures.kotlinConf2023.id,
                    conferenceName = TestFixtures.kotlinConf2023.name,
                    confDates = TestFixtures.kotlinConf2023.days,
                )
            ),
            bookmarksUiState = QueryResult.Success(
                BookmarksUiState(
                    conference = TestFixtures.kotlinConf2023.id,
                    upcoming = listOf(
                        TestFixtures.sessionDetails
                    ),
                    past = listOf(),
                    now = LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime()
                )
            ),
            sessionSelected = {},
            onSettingsClick = {},
            onBookmarksClick = {},
            daySelected = {},
            addBookmark = {},
            removeBookmark = {}
        )
    }
}
