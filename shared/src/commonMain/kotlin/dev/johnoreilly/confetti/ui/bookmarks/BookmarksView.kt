@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package dev.johnoreilly.confetti.ui.bookmarks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.bookmarks_past
import confetti.shared.generated.resources.bookmarks_upcoming
import confetti.shared.generated.resources.no_bookmarks
import dev.johnoreilly.confetti.decompose.DateSessionsMap
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.isService
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.preview.bookmarkedSessionsByDate
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.ui.LocalBottomNavigationPadding
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.component.EmptyView
import dev.johnoreilly.confetti.ui.component.LoadingView
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import dev.johnoreilly.confetti.ui.components.SessionCardModel
import dev.johnoreilly.confetti.preview.ComponentsPreview
import dev.johnoreilly.confetti.listTags
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
context(components: ConferenceComponents)
fun BookmarksView(
    conference: String,
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
            conference = conference,
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
context(components: ConferenceComponents)
private fun BookmarksContent(
    conference: String,
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
            BookmarksTab.entries.size
        }

        BookmarksTabRow(pagerState = pagerState)
        BookmarksHorizontalPager(
            conference = conference,
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

private enum class BookmarksTab(val title: @Composable () -> String) {
    Past(title = { stringResource(resource = Res.string.bookmarks_past) }),
    Upcoming(title = { stringResource(resource = Res.string.bookmarks_upcoming) })
}

@Composable
private fun BookmarksTabRow(pagerState: PagerState) {
    PrimaryTabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(pagerState.currentPage, matchContentSize = true),
            )
        },
        divider = {},
    ) {
        for ((index, tab) in BookmarksTab.entries.withIndex()) {
            val tabScope = rememberCoroutineScope()
            val selected = pagerState.currentPage == index

            Tab(
                selected = selected,
                onClick = {
                    tabScope.launch { pagerState.animateScrollToPage(index) }
                },
                text = {
                    Text(
                        text = tab.title(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            )
        }
    }
}

@Composable
context(components: ConferenceComponents)
private fun BookmarksHorizontalPager(
    conference: String,
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

        if (displayedSessions.isEmpty()) {
            EmptyView(
                text = stringResource(Res.string.no_bookmarks),
                message = if (page == BookmarksTab.Upcoming.ordinal) {
                    "Tap the bookmark icon on any session to save it here."
                } else null,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = LocalBottomNavigationPadding.current)
            ) {

                displayedSessions.forEach { (dateTime, sessions) ->
                    stickyHeader {
                        components.SectionHeader(conference, dateTime.time.toString(), Icons.Filled.AccessTime)
                    }

                    itemsIndexed(sessions) { index, session ->
                        components.SessionCard(
                            SessionCardModel(
                                conference = conference,
                                session = session,
                                now = null,
                                isBookmarked = bookmarks.contains(session.id),
                                isLoggedIn = isLoggedIn,
                                trackColor = null,
                                tags = session.listTags(),
                                sessionSelected = navigateToSession,
                                addBookmark = addBookmark,
                                removeBookmark = removeBookmark,
                                onNavigateToSignIn = onSignIn,
                            )
                        )
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

@MobilePreviews
@Composable
internal fun BookmarksViewLoadedPreview() {
    ComponentsPreview {
        BookmarksView(
            conference = "kotlinconf2023",
            pastSessions = emptyMap(),
            upcomingSessions = bookmarkedSessionsByDate,
            navigateToSession = {},
            onSignIn = {},
            bookmarks = setOf(sessionDetails.id),
            addBookmark = {},
            removeBookmark = {},
            loading = false,
            isLoggedIn = true,
        )
    }
}

@Preview(name = "Empty", widthDp = 411, heightDp = 914, showBackground = true)
@Composable
internal fun BookmarksViewEmptyPreview() {
    ComponentsPreview {
        BookmarksView(
            conference = "kotlinconf2023",
            pastSessions = emptyMap(),
            upcomingSessions = emptyMap(),
            navigateToSession = {},
            onSignIn = {},
            bookmarks = emptySet(),
            addBookmark = {},
            removeBookmark = {},
            loading = false,
            isLoggedIn = true,
        )
    }
}

@Preview(name = "Loading", widthDp = 411, heightDp = 914, showBackground = true)
@Composable
internal fun BookmarksViewLoadingPreview() {
    ComponentsPreview {
        BookmarksView(
            conference = "kotlinconf2023",
            pastSessions = emptyMap(),
            upcomingSessions = emptyMap(),
            navigateToSession = {},
            onSignIn = {},
            bookmarks = emptySet(),
            addBookmark = {},
            removeBookmark = {},
            loading = true,
            isLoggedIn = false,
        )
    }
}
