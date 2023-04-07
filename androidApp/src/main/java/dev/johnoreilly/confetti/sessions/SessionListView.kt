@file:OptIn(ExperimentalFoundationApi::class)

package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.isLightning
import dev.johnoreilly.confetti.sessionSpeakers
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.SignInDialog
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.component.ConfettiTab
import dev.johnoreilly.confetti.ui.component.pagerTabIndicatorOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionListView(
    uiState: SessionsUiState,
    refreshing: Boolean,
    sessionSelected: (SessionDetailsKey) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    user: User?
) {
    val pagerState = rememberPagerState()

    when (uiState) {
        SessionsUiState.Error -> ErrorView(onRefresh)
        SessionsUiState.Loading -> LoadingView()
        is SessionsUiState.Success -> {
            val state = rememberPullRefreshState(refreshing, onRefresh)
            Column {

                SessionListTabRow(pagerState, uiState)

                HorizontalPager(
                    pageCount = uiState.formattedConfDates.size,
                    state = pagerState,
                ) { page ->

                    val sessions = uiState.sessionsByStartTimeList[page]
                    Box(
                        Modifier
                            .pullRefresh(state)
                            .clipToBounds()
                    ) {
                        LazyColumn {
                            sessions.forEach { (startTime, sessions) ->
                                stickyHeader {
                                    ConfettiHeader(icon = Icons.Filled.AccessTime, text = startTime)
                                }

                                items(sessions) { session ->
                                    SessionItemView(
                                        conference = uiState.conference,
                                        session = session,
                                        sessionSelected = sessionSelected,
                                        isBookmarked = uiState.bookmarks.contains(session.id),
                                        addBookmark = addBookmark,
                                        removeBookmark = removeBookmark,
                                        onNavigateToSignIn = onNavigateToSignIn,
                                        user,
                                    )
                                }
                            }
                        }
                        PullRefreshIndicator(
                            refreshing,
                            state,
                            Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionListTabRow(pagerState: PagerState, uiState: SessionsUiState.Success) {
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        }
    ) {
        uiState.formattedConfDates.forEachIndexed { index, formattedDate ->
            val coroutineScope = rememberCoroutineScope()

            ConfettiTab(
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = { Text(text = formattedDate) }
            )
        }
    }
}

@Composable
fun SessionItemView(
    conference: String,
    session: SessionDetails,
    sessionSelected: (SessionDetailsKey) -> Unit,
    isBookmarked: Boolean,
    addBookmark: (String) -> Unit,
    removeBookmark: (String) -> Unit,
    onNavigateToSignIn: () -> Unit = {},
    user: User?,
) {

    var modifier = Modifier.fillMaxSize()
    if (!session.isBreak()) {
        modifier = modifier.clickable(onClick = {
            sessionSelected(SessionDetailsKey(conference, session.id))
        })
    }
    Row(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = session.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }

            session.room?.let { room ->
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        session.sessionSpeakers() ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        room.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            if (session.isLightning()) {
                Surface(
                    modifier = Modifier.padding(top = 8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                        Icon(Icons.Default.Bolt, "lightning")
                        Spacer(Modifier.width(4.dp))
                        Text("Lightning / ${session.startsAt.time}-${session.endsAt.time}")
                    }
                }
            }
        }


        var showDialog by remember { mutableStateOf(false) }

        if (isBookmarked) {
            Icon(
                imageVector = Icons.Outlined.Bookmark,
                contentDescription = "remove bookmark",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable {
                        if (user != null) {
                            removeBookmark(session.id)
                        } else {
                            showDialog = true
                        }
                    }
                    .padding(8.dp))
        } else {
            Icon(
                imageVector = Icons.Outlined.BookmarkAdd,
                contentDescription = "add bookmark",
                modifier = Modifier
                    .clickable {
                        if (user != null) {
                            addBookmark(session.id)
                        } else {
                            showDialog = true
                        }
                    }
                    .padding(8.dp))
        }

        if (showDialog) {
            SignInDialog(
                onDismissRequest = { showDialog = false },
                onSignInClicked = onNavigateToSignIn
            )
        }
    }
}
