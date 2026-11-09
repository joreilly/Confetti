@file:OptIn(ExperimentalFoundationApi::class)

package dev.johnoreilly.confetti.ui.sessions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import org.jetbrains.compose.ui.tooling.preview.Preview
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.preview.sessionsSuccessState
import dev.johnoreilly.confetti.ui.LocalTopBarCollapsedFraction
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.component.ErrorView
import dev.johnoreilly.confetti.ui.component.LoadingView
import dev.johnoreilly.confetti.ui.icons.AccessTime
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import kotlin.math.abs

import androidx.compose.foundation.layout.PaddingValues
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.isService
import dev.johnoreilly.confetti.listTags
import dev.johnoreilly.confetti.ui.LocalBottomNavigationPadding
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import dev.johnoreilly.confetti.ui.components.SessionCardModel
import dev.johnoreilly.confetti.preview.ComponentsPreview
import dev.johnoreilly.confetti.ui.trackColor
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

/**
 * The agenda. The conference header, its promotion and the session cards are drawn by [components], the
 * context the screen's entry point supplies: Confetti's own composables, or a conference's designs.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
context(components: ConferenceComponents)
fun SessionListView(

    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onTrackSelectionChanged: (Set<String>) -> Unit,
    isLoggedIn: Boolean,
) {
    when (uiState) {
        SessionsUiState.Error -> ErrorView(onRefresh = onRefresh)
        SessionsUiState.Loading -> LoadingView()
        is SessionsUiState.Success -> {
            Column {
                val initialPageIndex by remember {
                    derivedStateOf { uiState.confDates.indexOf(uiState.now.date) }
                }

                val pagerState = rememberPagerState(
                    if (initialPageIndex == -1) 0 else initialPageIndex
                ) {
                    uiState.formattedConfDates.size
                }
                val trackNames = remember(uiState.tracks) { uiState.tracks.map { it.name }.toSet() }
                // Items above the first sticky header: the conference's header and its home-feed
                // promotion, each when it has one.
                val hasHeader = components.hasConferenceHeader(uiState.conference)
                val hasPromo = components.hasPromo(HOME_FEED, uiState.conference)
                val headerItems = (if (hasHeader) 1 else 0) + (if (hasPromo) 1 else 0)

                AnimatedVisibility(visible = LocalTopBarCollapsedFraction.current < 0.5f) {
                    TrackFilterRow(
                        tracks = uiState.tracks,
                        selectedTracks = uiState.selectedTracks,
                        onTrackSelectionChanged = onTrackSelectionChanged,
                    )
                }
                SessionListTabRow(pagerState, uiState)

                HorizontalPager(
                    state = pagerState,
                    // workaround for collapsing toolbar glicthing during the nested pager's lazy list scroll.
                    pageNestedScrollConnection = remember { object : NestedScrollConnection {} },
                ) { page ->
                    val sessions = uiState.sessionsByStartTimeList[page]

                    val initialItemIndex by remember {
                        derivedStateOf {
                            // If initial page is null, we are in the wrong date and should not
                            // consider the current hour.
                            if (initialPageIndex != page) return@derivedStateOf 0

                            // Retrieves the initial item matching an hour block, including the
                            // aggregated index (ignores time grouping).
                            val initialItem = sessions
                                .values
                                .flatten()
                                .withIndex()
                                .minByOrNull { (_, session) ->
                                    val timeOfNowInMillis = uiState
                                        .now
                                        .time
                                        .toMillisecondOfDay()
                                    val timeOfSessionStartInMillis = session
                                        .startsAt
                                        .time
                                        .toMillisecondOfDay()
                                    abs(timeOfNowInMillis - timeOfSessionStartInMillis)
                                }

                            // Count the number of sticky headers until the initial item.
                            val stickyHeader = sessions
                                .entries
                                .withIndex()
                                .firstOrNull {
                                    it.value.value.contains(initialItem?.value)
                                }

                            val stickyHeaderIndex = stickyHeader?.index ?: 0
                            val initialItemIndex = initialItem?.index ?: 0

                            // Sum the index of the initial item and the sticky headers, after
                            // any conference header at the top.
                            headerItems + stickyHeaderIndex + initialItemIndex
                        }
                    }

                    val listState = rememberLazyListState(initialItemIndex)

                    Box(
                        Modifier
                            .clipToBounds()
                    ) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(bottom = LocalBottomNavigationPadding.current)
                        ) {
                            // The conference's header, scrolling with the agenda: no item when
                            // it has none.
                            if (hasHeader) {
                                item(key = "conference-header") {
                                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                        components.ConferenceHeader(uiState)
                                    }
                                }
                            }
                            // A promotion on the home feed, likewise its own item.
                            if (hasPromo) {
                                item(key = "promo-$HOME_FEED") {
                                    components.Promo(HOME_FEED, uiState.conference, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
                                }
                            }

                            sessions.forEach { (startTime, sessions) ->

                                stickyHeader {
                                    components.SectionHeader(uiState.conference, startTime, ConfettiIcons.AccessTime)
                                }

                                itemsIndexed(sessions) { index, session ->
                                    val card = SessionCardModel(
                                        conference = uiState.conference,
                                        session = session,
                                        now = uiState.now,
                                        isBookmarked = uiState.bookmarks.contains(session.id),
                                        isLoggedIn = isLoggedIn,
                                        trackColor = session.trackColor(uiState.tracks),
                                        tags = session.listTags(trackNames, uiState.selectedTracks),
                                        sessionSelected = sessionSelected,
                                        addBookmark = addBookmark,
                                        removeBookmark = removeBookmark,
                                        onNavigateToSignIn = onNavigateToSignIn,
                                    )
                                    components.SessionCard(card)
                                    val isCurrentBreak = session.isBreak() || session.isService()
                                    val isNextBreak = index < sessions.lastIndex && (sessions[index + 1].isBreak() || sessions[index + 1].isService())
                                    if (index < sessions.lastIndex && !isCurrentBreak && !isNextBreak) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@MobilePreviews
@Composable
internal fun SessionListViewLoadedPreview() {
    ComponentsPreview {
        SessionListView(
            uiState = sessionsSuccessState,
            sessionSelected = {},
            addBookmark = {},
            removeBookmark = {},
            onRefresh = {},
            onNavigateToSignIn = {},
            onTrackSelectionChanged = {},
            isLoggedIn = false,
        )
    }
}

@Preview(name = "Loading", widthDp = 411, heightDp = 914, showBackground = true)
@Composable
internal fun SessionListViewLoadingPreview() {
    ComponentsPreview {
        SessionListView(
            uiState = SessionsUiState.Loading,
            sessionSelected = {},
            addBookmark = {},
            removeBookmark = {},
            onRefresh = {},
            onNavigateToSignIn = {},
            onTrackSelectionChanged = {},
            isLoggedIn = false,
        )
    }
}

@Preview(name = "Error", widthDp = 411, heightDp = 914, showBackground = true)
@Composable
internal fun SessionListViewErrorPreview() {
    ComponentsPreview {
        SessionListView(
            uiState = SessionsUiState.Error,
            sessionSelected = {},
            addBookmark = {},
            removeBookmark = {},
            onRefresh = {},
            onNavigateToSignIn = {},
            onTrackSelectionChanged = {},
            isLoggedIn = false,
        )
    }
}

/** The agenda's promotion placement. */
private const val HOME_FEED = "home.feed"
