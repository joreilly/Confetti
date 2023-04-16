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
import androidx.compose.ui.res.stringResource
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.sessions.SessionItemView
import dev.johnoreilly.confetti.ui.ConfettiAppState
import dev.johnoreilly.confetti.ui.ConfettiScaffold
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.component.ConfettiTab
import dev.johnoreilly.confetti.utils.format
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun BookmarksView(
    conference: String,
    appState: ConfettiAppState,
    pastSessions: List<SessionDetails>,
    upcomingSessions: List<SessionDetails>,
    navigateToSession: (SessionDetailsKey) -> Unit,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    loading: Boolean,
) {
    ConfettiScaffold(
        title = stringResource(R.string.bookmarks),
        conference = conference,
        appState = appState,
        onSwitchConference = onSwitchConference,
        onSignIn = onSignIn,
        onSignOut = onSignOut,
    ) { state ->
        val user = state.user
        Column {

            if (loading) {
                LoadingView()
            } else {
                BookmarksContent(
                    pastSessions = pastSessions,
                    upcomingSessions = upcomingSessions,
                    conference = conference,
                    navigateToSession = navigateToSession,
                    bookmarks = bookmarks,
                    addBookmark = addBookmark,
                    removeBookmark = removeBookmark,
                    onSignIn = onSignIn,
                    user = user,
                )

            }
        }
    }
}

@Composable
private fun BookmarksContent(
    pastSessions: List<SessionDetails>,
    upcomingSessions: List<SessionDetails>,
    conference: String,
    navigateToSession: (SessionDetailsKey) -> Unit,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onSignIn: () -> Unit,
    user: User?
) {
    Column {
        val pagerState = rememberPagerState(initialPage = 1)
        BookmarksTabRow(pagerState = pagerState)
        BookmarksHorizontalPager(
            pagerState = pagerState,
            pastSessions = pastSessions,
            upcomingSessions = upcomingSessions,
            conference = conference,
            navigateToSession = navigateToSession,
            bookmarks = bookmarks,
            addBookmark = addBookmark,
            removeBookmark = removeBookmark,
            onSignIn = onSignIn,
            user = user,
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
    pastSessions: List<SessionDetails>,
    upcomingSessions: List<SessionDetails>,
    conference: String,
    navigateToSession: (SessionDetailsKey) -> Unit,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onSignIn: () -> Unit,
    user: User?
) {
    HorizontalPager(
        pageCount = BookmarksTab.values().size,
        state = pagerState,
    ) { page ->
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
            val grouped = displayedSessions.groupBy { it.startsAt }

            grouped.forEach { (localTime, sessions) ->
                stickyHeader {
                    ConfettiHeader(
                        icon = Icons.Filled.AccessTime,
                        text = DateTimeFormatter.ofPattern("MMM d, HH:mm").format(localTime)
                    )
                }

                items(sessions) { session ->
                    SessionItemView(
                        conference = conference,
                        session = session,
                        sessionSelected = navigateToSession,
                        isBookmarked = bookmarks.contains(session.id),
                        addBookmark = { sessionId -> addBookmark(sessionId) },
                        removeBookmark = { sessionId -> removeBookmark(sessionId) },
                        onNavigateToSignIn = onSignIn,
                        user = user,
                    )
                }
            }
        }
    }
}
