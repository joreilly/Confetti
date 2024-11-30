@file:OptIn(ExperimentalFoundationApi::class)

package dev.johnoreilly.confetti.ui.sessions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.component.ErrorView
import dev.johnoreilly.confetti.ui.component.LoadingView
import dev.johnoreilly.confetti.ui.icons.AccessTime
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionListView(
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    isLoggedIn: Boolean,
) {
    when (uiState) {
        SessionsUiState.Error -> ErrorView(onRefresh)
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

                            // Sum the index of the initial item and the sticky headers.
                            stickyHeaderIndex + initialItemIndex
                        }
                    }

                    val listState = rememberLazyListState(initialItemIndex)

                    Box(
                        Modifier
                            .clipToBounds()
                    ) {
                        LazyColumn(state = listState) {
                            sessions.forEach { (startTime, sessions) ->

                                stickyHeader {
                                    ConfettiHeader(icon = ConfettiIcons.AccessTime, text = startTime)
                                }

                                val sortedSessions =
                                    sessions.sortedBy { session -> uiState.rooms.indexOfFirst { it.name == session.room?.name } }
                                items(sortedSessions) { session ->
                                    SessionItemView(
                                        session = session,
                                        sessionSelected = sessionSelected,
                                        isBookmarked = uiState.bookmarks.contains(session.id),
                                        addBookmark = addBookmark,
                                        removeBookmark = removeBookmark,
                                        onNavigateToSignIn = onNavigateToSignIn,
                                        isLoggedIn = isLoggedIn,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
