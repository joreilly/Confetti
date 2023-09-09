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
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedChip
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.rememberPlaceholderState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.PlaceholderChip
import com.google.android.horologist.composables.Section
import com.google.android.horologist.composables.Section.Companion.ALL_STATES
import com.google.android.horologist.composables.Section.Companion.NO_STATES
import com.google.android.horologist.composables.SectionedList
import com.google.android.horologist.composables.SectionedListScope
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.material.Button
import com.google.android.horologist.compose.material.Chip
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
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
    onBookmarksClick: () -> Unit,
    columnState: ScalingLazyColumnState
) {
    val dayFormatter = remember { DateTimeFormatter.ofPattern("cccc") }

    SectionedList(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        titleSection(uiState)

        bookmarksSection(uiState, bookmarksUiState, sessionSelected, onBookmarksClick)

        conferenceDaysSection(uiState, daySelected, dayFormatter)

        bottomMenuSection(onSettingsClick)
    }
}

private fun SectionedListScope.titleSection(uiState: QueryResult<HomeUiState>) {
    val titleSectionState = when (uiState) {
        is QueryResult.Success -> Section.State.Loaded(listOf(uiState.result.conferenceName))
        QueryResult.Loading -> Section.State.Loading
        is QueryResult.Error -> Section.State.Failed
        QueryResult.None -> Section.State.Empty
    }

    section(state = titleSectionState) {
        loaded { conferenceName ->
            ConferenceTitle(conferenceName)
        }

        loading {
            val chipPlaceholderState = rememberPlaceholderState { false }
            SectionHeader(
                "",
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .placeholder(chipPlaceholderState)
            )
        }
    }
}

private fun SectionedListScope.bookmarksSection(
    uiState: QueryResult<HomeUiState>,
    bookmarksUiState: QueryResult<BookmarksUiState>,
    sessionSelected: (String) -> Unit,
    onBookmarksClick: () -> Unit
) {
    val bookmarksSectionState = when (bookmarksUiState) {
        is QueryResult.Success -> {
            if (bookmarksUiState.result.hasUpcomingBookmarks) {
                Section.State.Loaded(bookmarksUiState.result.upcoming.take(3))
            } else {
                Section.State.Empty
            }
        }

        QueryResult.Loading -> Section.State.Loading
        is QueryResult.Error -> Section.State.Failed
        QueryResult.None -> Section.State.Failed // handling "None" as a failure
    }

    section(state = bookmarksSectionState) {
        header(visibleStates = ALL_STATES.copy(failed = false)) {
            SectionHeader(stringResource(R.string.home_bookmarked_sessions))
        }

        loaded { session ->
            key(session.id) {
                SessionCard(session, sessionSelected = {
                    if (uiState is QueryResult.Success) {
                        sessionSelected(it)
                    }
                }, (bookmarksUiState as QueryResult.Success).result.now)
            }
        }

        // TODO placeholders
        // loading {}

        empty {
            Text(stringResource(id = R.string.no_upcoming))
        }


        footer(visibleStates = NO_STATES.copy(loaded = true, empty = true)) {
            OutlinedChip(
                label = { Text(stringResource(id = R.string.all_bookmarks)) },
                onClick = {
                    if (uiState is QueryResult.Success) {
                        onBookmarksClick()
                    }
                }
            )
        }
    }
}

private fun SectionedListScope.conferenceDaysSection(
    uiState: QueryResult<HomeUiState>,
    daySelected: (LocalDate) -> Unit,
    dayFormatter: DateTimeFormatter
) {
    val conferenceDaysSectionState = when (uiState) {
        is QueryResult.Success -> Section.State.Loaded(uiState.result.confDates)
        QueryResult.Loading -> Section.State.Loading
        is QueryResult.Error -> Section.State.Failed
        QueryResult.None -> Section.State.Empty
    }

    section(state = conferenceDaysSectionState) {
        header {
            SectionHeader(stringResource(id = R.string.conference_days))
        }

        loaded { date ->
            // TODO format date
            Chip(
                label = dayFormatter.format(date.toJavaLocalDate()),
                onClick = {
                    daySelected(
                        date
                    )
                },
                colors = ChipDefaults.secondaryChipColors()
            )
        }

        loading(count = 2) {
            PlaceholderChip(contentDescription = "")
        }
    }
}

private fun SectionedListScope.bottomMenuSection(onSettingsClick: () -> Unit) {
    section {
        loaded {
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
