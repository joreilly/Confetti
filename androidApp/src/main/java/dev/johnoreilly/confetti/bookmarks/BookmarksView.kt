@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package dev.johnoreilly.confetti.bookmarks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dev.johnoreilly.confetti.decompose.DateSessionsMap
import dev.johnoreilly.confetti.sessions.SessionItemView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.component.ConfettiTab
import dev.johnoreilly.confetti.utils.format
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun BookmarksView(
    pastSessions: DateSessionsMap,
    upcomingSessions: DateSessionsMap,
    navigateToSession: (id: String) -> Unit,
    onSignIn: () -> Unit,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    loading: Boolean,
    isLoggedIn: Boolean,
) {
    if (loading) {
        LoadingView()
    } else {
        BookmarksContent(
            pastSessions = pastSessions,
            upcomingSessions = upcomingSessions,
            navigateToSession = navigateToSession,
            bookmarks = bookmarks,
            addBookmark = addBookmark,
            removeBookmark = removeBookmark,
            onSignIn = onSignIn,
            isLoggedIn = isLoggedIn,
        )
    }
}

@Composable
private fun BookmarksContent(
    pastSessions: DateSessionsMap,
    upcomingSessions: DateSessionsMap,
    navigateToSession: (id: String) -> Unit,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onSignIn: () -> Unit,
    isLoggedIn: Boolean,
) {
    Column {
        val pagerState = rememberPagerState(initialPage = 1) {
            BookmarksTab.values().size
        }

        BookmarksTabRow(pagerState = pagerState)
        BookmarksHorizontalPager(
            pagerState = pagerState,
            pastSessions = pastSessions,
            upcomingSessions = upcomingSessions,
            navigateToSession = navigateToSession,
            bookmarks = bookmarks,
            addBookmark = addBookmark,
            removeBookmark = removeBookmark,
            onSignIn = onSignIn,
            isLoggedIn = isLoggedIn,
        )
    }
}

private enum class BookmarksTab(val title: String) {
    Past(title = "Past"),
    Upcoming(title = "Upcoming")
}

@Composable
private fun BookmarksTabRow(pagerState: PagerState) {
    TabRow(selectedTabIndex = pagerState.currentPage) {
        for ((index, tab) in BookmarksTab.values().withIndex()) {
            val tabScope = rememberCoroutineScope()

            ConfettiTab(
                selected = pagerState.currentPage == index,
                onClick = {
                    tabScope.launch { pagerState.animateScrollToPage(index) }
                },
                text = { Text(text = tab.title) }
            )
        }
    }
}

@Composable
private fun BookmarksHorizontalPager(
    pagerState: PagerState,
    pastSessions: DateSessionsMap,
    upcomingSessions: DateSessionsMap,
    navigateToSession: (id: String) -> Unit,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onSignIn: () -> Unit,
    isLoggedIn: Boolean,
) {
    HorizontalPager(state = pagerState) { page ->
        val displayedSessions =
            if (page == BookmarksTab.Past.ordinal) {
                pastSessions
            } else {
                upcomingSessions
            }

        LazyColumn(
            contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                .asPaddingValues()
        ) {

            displayedSessions.forEach { (dateTime, sessions) ->
                stickyHeader {
                    ConfettiHeader(
                        icon = Icons.Filled.AccessTime,
                        text = DateTimeFormatter.ofPattern("MMM d, HH:mm").format(dateTime)
                    )
                }

                items(sessions) { session ->
                    SessionItemView(
                        session = session,
                        sessionSelected = navigateToSession,
                        isBookmarked = bookmarks.contains(session.id),
                        addBookmark = { sessionId -> addBookmark(sessionId) },
                        removeBookmark = { sessionId -> removeBookmark(sessionId) },
                        onNavigateToSignIn = onSignIn,
                        isLoggedIn = isLoggedIn,
                    )
                }
            }
        }
    }
}
