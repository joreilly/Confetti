@file:OptIn(ExperimentalHorologistApi::class, ExperimentalWearMaterialApi::class)

package dev.johnoreilly.confetti.wear.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedChip
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.rememberPlaceholderState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.PlaceholderChip
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.material.Button
import com.google.android.horologist.compose.material.Chip
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    uiState: QueryResult<HomeUiState>,
    bookmarksUiState: QueryResult<BookmarksUiState>,
    sessionSelected: (SessionDetailsKey) -> Unit,
    daySelected: (ConferenceDayKey) -> Unit,
    onSettingsClick: () -> Unit,
    onBookmarksClick: (String) -> Unit,
    columnState: ScalingLazyColumnState
) {
    val dayFormatter = remember { DateTimeFormatter.ofPattern("cccc") }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        item {
            if (uiState is QueryResult.Success) {
                ConferenceTitle(uiState.result.conferenceName)
            } else if (uiState is QueryResult.Loading) {
                val chipPlaceholderState = rememberPlaceholderState { false }
                SectionHeader(
                    "",
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .placeholder(chipPlaceholderState)
                )
            }
        }

        if (bookmarksUiState !is QueryResult.None) {
            item {
                SectionHeader(stringResource(R.string.home_bookmarked_sessions))
            }
        }

        if (bookmarksUiState is QueryResult.Success) {
            val now = bookmarksUiState.result.now

            items(bookmarksUiState.result.upcoming.take(3)) { session ->
                key(session.id) {
                    SessionCard(session, sessionSelected = {
                        if (uiState is QueryResult.Success) {
                            sessionSelected(SessionDetailsKey(uiState.result.conference, it))
                        }
                    }, now)
                }
            }

            if (!bookmarksUiState.result.hasUpcomingBookmarks) {
                item {
                    Text(stringResource(id = R.string.no_upcoming))
                }
            }

            item {
                OutlinedChip(
                    label = { Text(stringResource(id = R.string.all_bookmarks)) },
                    onClick = {
                        if (uiState is QueryResult.Success) {
                            onBookmarksClick(uiState.result.conference)
                        }
                    }
                )
            }
        } else if (uiState is QueryResult.Loading) {
            // TODO placeholders
        }

        item {
            SectionHeader(stringResource(id = R.string.conference_days))
        }
        if (uiState is QueryResult.Success) {
            items(uiState.result.confDates.size) {
                // TODO format date
                val date = uiState.result.confDates[it]
                Chip(
                    label = dayFormatter.format(date.toJavaLocalDate()),
                    onClick = { daySelected(ConferenceDayKey(uiState.result.conference, date)) },
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
        } else if (uiState is QueryResult.Loading) {
            items(2) {
                PlaceholderChip(contentDescription = "")
            }
        }

        item {
            Button(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.home_settings_content_description),
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
fun ConferenceTitle(conferenceName: String) {
    Text(
        text = conferenceName,
        modifier = Modifier
            .semantics {
                heading()
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.title3,
    )
}

@WearPreviewDevices
@WearPreviewFontSizes
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
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            sessionSelected = {},
            onSettingsClick = {},
            onBookmarksClick = {},
            daySelected = {},
        )
    }
}
