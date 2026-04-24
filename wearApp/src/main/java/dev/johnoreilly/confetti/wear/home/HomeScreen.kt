package dev.johnoreilly.confetti.wear.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.PlaceholderState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.wear.compose.ui.tooling.preview.WearPreviewLargeRound
import ee.schimke.composeai.preview.ScrollMode
import ee.schimke.composeai.preview.ScrollingPreview
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import dev.johnoreilly.confetti.wear.components.PlaceholderButton
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.preview.ConferenceFixtures
import dev.johnoreilly.confetti.wear.preview.ConfettiPreviewScaffold
import dev.johnoreilly.confetti.wear.ui.conferenceThemeFor
import dev.johnoreilly.confetti.wear.preview.TestFixtures
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
    modifier: Modifier = Modifier,
) {
    val dayFormatter = remember { DateTimeFormatter.ofPattern("cccc") }

    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val placeholderState = rememberPlaceholderState(uiState is QueryResult.Loading)
    ScreenScaffold(
        modifier = modifier,
        scrollState = columnState,
        scrollIndicator = {
            if (!LocalScrollCaptureInProgress.current) {
                ScrollIndicator(columnState)
            }
        },
        edgeButton = {
            EdgeButton(
                onClick = onSettingsClick,
                buttonSize = EdgeButtonSize.Small,
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.home_settings_content_description),
                )
            }
        },
    ) { contentPadding ->
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = contentPadding,
        ) {
            titleSection(
                uiState = uiState,
                placeholderState = placeholderState,
                transformationSpec = transformationSpec,
            )

            bookmarksSection(
                uiState = uiState,
                bookmarksUiState = bookmarksUiState,
                sessionSelected = sessionSelected,
                addBookmark = addBookmark,
                removeBookmark = removeBookmark,
                onBookmarksClick = onBookmarksClick,
                transformationSpec = transformationSpec,
            )

            conferenceDaysSection(
                uiState = uiState,
                daySelected = daySelected,
                dayFormatter = dayFormatter,
                transformationSpec = transformationSpec,
            )
        }
    }
}

private fun TransformingLazyColumnScope.titleSection(
    uiState: QueryResult<HomeUiState>,
    placeholderState: PlaceholderState,
    transformationSpec: TransformationSpec,
) {
    when (uiState) {
        is QueryResult.Success, QueryResult.Loading -> {
            item {
                val success = uiState as? QueryResult.Success
                val conferenceTheme = conferenceThemeFor(success?.result?.conference)
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                        .minimumVerticalContentPadding(
                            ListHeaderDefaults.minimumTopListContentPadding
                        ),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        // A small primary-tinted icon above the conference
                        // name acts as a brand anchor — one visual cue (+ the
                        // seedColor tint) to tell KotlinConf from Droidcon
                        // from DevFest at a glance. Unknown conferences skip
                        // the icon rather than showing a generic stand-in.
                        if (conferenceTheme != null && success != null) {
                            Icon(
                                imageVector = conferenceTheme.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        // Conference name is the app's headline on this screen
                        // — carry the seedColor forward via `primary` and
                        // pick the largest legible Wear title style so the
                        // chosen typography reads here first.
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .placeholderShimmer(placeholderState)
                                .placeholder(placeholderState),
                            text = success?.result?.conferenceName ?: " \n ",
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
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
    onBookmarksClick: () -> Unit,
    transformationSpec: TransformationSpec,
) {
    item {
        SectionHeader(
            modifier = Modifier
                .fillMaxWidth()
                .transformedHeight(this, transformationSpec),
            text = stringResource(R.string.home_bookmarked_sessions),
            transformation = SurfaceTransformation(transformationSpec),
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
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(
                                CardDefaults.minimumVerticalListContentPadding
                            ),
                        session = session, sessionSelected = {
                            if (uiState is QueryResult.Success) {
                                sessionSelected(it)
                            }
                        },
                        currentTime = bookmarksUiState.result.now,
                        addBookmark = addBookmark,
                        removeBookmark = removeBookmark,
                        isBookmarked = bookmarksUiState.result.isBookmarked(session.id),
                        transformation = SurfaceTransformation(transformationSpec),
                    )
                }
            }
            if (upcoming.isEmpty()) {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                with(transformationSpec) {
                                    applyContainerTransformation(scrollProgress)
                                }
                            }
                            .transformedHeight(this, transformationSpec),
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
                .transformedHeight(this, transformationSpec)
                .minimumVerticalContentPadding(
                    ButtonDefaults.minimumVerticalListContentPadding
                ),
            transformation = SurfaceTransformation(transformationSpec),
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
    dayFormatter: DateTimeFormatter,
    transformationSpec: TransformationSpec,
) {
    item {
        SectionHeader(
            modifier = Modifier
                .fillMaxWidth()
                .transformedHeight(this, transformationSpec),
            text = stringResource(id = R.string.conference_days),
            transformation = SurfaceTransformation(transformationSpec),
        )
    }
    when (uiState) {
        is QueryResult.Success -> {
            items(uiState.result.confDates) { date ->
                DayChip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                        .minimumVerticalContentPadding(
                            ButtonDefaults.minimumVerticalListContentPadding
                        ),
                    dayFormatter,
                    date,
                    daySelected = { daySelected(date) },
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
        }

        QueryResult.Loading -> {
            items(2) {
                PlaceholderButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                        .minimumVerticalContentPadding(
                            ButtonDefaults.minimumVerticalListContentPadding
                        ),
                    transformation = SurfaceTransformation(transformationSpec),
                )
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
    daySelected: () -> Unit,
    transformation: SurfaceTransformation? = null,
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        transformation = transformation,
        onClick = daySelected,
        colors = ButtonDefaults.filledVariantButtonColors(),
    ) {
        Text(dayFormatter.format(date.toJavaLocalDate()))
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun HomeListViewPreview() {
    ConfettiPreviewScaffold {
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

@WearPreviewLargeRound
@ScrollingPreview(
    modes = [ScrollMode.TOP, ScrollMode.END, ScrollMode.LONG, ScrollMode.GIF],
    reduceMotion = false,
)
@Composable
fun HomeListViewLongPreview() {
    ConfettiPreviewScaffold {
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
                        TestFixtures.sessionDetails,
                        TestFixtures.sessionDetails.copy(id = "2", title = "Advanced Coroutines"),
                        TestFixtures.sessionDetails.copy(id = "3", title = "Compose Multiplatform"),
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

/**
 * Per-conference HomeScreen previews. Each renders a realistic schedule
 * snapshot under the curated seedColor (and, for DevFest, the Google Sans
 * Flex typography override) wired up by [conferenceThemeFor]. These are the
 * visuals referenced in `design/STYLE_GUIDE.md` §4.
 */
@WearPreviewLargeRound
@ScrollingPreview(modes = [ScrollMode.LONG])
@Composable
fun HomeListViewKotlinConf() {
    val theme = conferenceThemeFor(ConferenceFixtures.kotlinConf.id)
    ConfettiPreviewScaffold(
        seedColor = theme?.seedColor,
        typography = theme?.typography ?: dev.johnoreilly.confetti.wear.ui.ExpressiveTypography,
    ) {
        HomeScreen(
            uiState = QueryResult.Success(ConferenceFixtures.kotlinConfHome),
            bookmarksUiState = QueryResult.Success(ConferenceFixtures.kotlinConfBookmarks),
            sessionSelected = {}, onSettingsClick = {}, onBookmarksClick = {},
            daySelected = {}, addBookmark = {}, removeBookmark = {},
        )
    }
}

@WearPreviewLargeRound
@ScrollingPreview(modes = [ScrollMode.LONG])
@Composable
fun HomeListViewAndroidMakers() {
    ConfettiPreviewScaffold(seedColor = conferenceThemeFor(ConferenceFixtures.androidMakers.id)?.seedColor) {
        HomeScreen(
            uiState = QueryResult.Success(ConferenceFixtures.androidMakersHome),
            bookmarksUiState = QueryResult.Success(ConferenceFixtures.androidMakersBookmarks),
            sessionSelected = {}, onSettingsClick = {}, onBookmarksClick = {},
            daySelected = {}, addBookmark = {}, removeBookmark = {},
        )
    }
}

@WearPreviewLargeRound
@ScrollingPreview(modes = [ScrollMode.LONG])
@Composable
fun HomeListViewDroidcon() {
    ConfettiPreviewScaffold(seedColor = conferenceThemeFor(ConferenceFixtures.droidcon.id)?.seedColor) {
        HomeScreen(
            uiState = QueryResult.Success(ConferenceFixtures.droidconHome),
            bookmarksUiState = QueryResult.Success(ConferenceFixtures.droidconBookmarks),
            sessionSelected = {}, onSettingsClick = {}, onBookmarksClick = {},
            daySelected = {}, addBookmark = {}, removeBookmark = {},
        )
    }
}

@WearPreviewLargeRound
@ScrollingPreview(modes = [ScrollMode.LONG])
@Composable
fun HomeListViewDevFest() {
    val theme = conferenceThemeFor(ConferenceFixtures.devFest.id)
    ConfettiPreviewScaffold(
        seedColor = theme?.seedColor,
        typography = theme?.typography ?: dev.johnoreilly.confetti.wear.ui.ExpressiveTypography,
    ) {
        HomeScreen(
            uiState = QueryResult.Success(ConferenceFixtures.devFestHome),
            bookmarksUiState = QueryResult.Success(ConferenceFixtures.devFestBookmarks),
            sessionSelected = {}, onSettingsClick = {}, onBookmarksClick = {},
            daySelected = {}, addBookmark = {}, removeBookmark = {},
        )
    }
}

