@file:OptIn(ExperimentalFoundationApi::class)

package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.account.Authentication
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.sessionSpeakerLocation
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.component.ConfettiTab
import dev.johnoreilly.confetti.ui.component.pagerTabIndicatorOffset
import dev.johnoreilly.confetti.utils.format
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionListView(
    uiState: SessionsUiState,
    refreshing: Boolean,
    sessionSelected: (SessionDetailsKey) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onRefresh: () -> Unit
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
                    pageCount = uiState.confDates.size,
                    state = pagerState,
                ) { page ->

                    val sessions = uiState.sessionsByStartTimeList[page]
                    Box(
                        Modifier
                            .pullRefresh(state)
                            .clipToBounds()
                    ) {
                        LazyColumn {
                            sessions.forEach {
                                item {
                                    Column(
                                        Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 16.dp,
                                            bottom = 8.dp
                                        )
                                    ) {
                                        Text(
                                            it.key,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Divider()
                                    }
                                }

                                items(it.value) { session ->
                                    SessionView(
                                        uiState.conference,
                                        session,
                                        sessionSelected,
                                        uiState.bookmarks.contains(session.id),
                                        addBookmark,
                                        removeBookmark
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

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

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
        uiState.confDates.forEachIndexed { index, date ->
            val coroutineScope = rememberCoroutineScope()

            ConfettiTab(
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = { Text(text = dateFormatter.format(date)) }
            )
        }
    }
}

@Composable
fun SessionView(
    conference: String,
    session: SessionDetails,
    sessionSelected: (SessionDetailsKey) -> Unit,
    isBookmarked: Boolean,
    addBookmark: (String) -> Unit,
    removeBookmark: (String) -> Unit
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
                Text(text = session.title, style = TextStyle(fontSize = 16.sp))
            }

            session.room?.let {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        session.sessionSpeakerLocation(),
                        style = TextStyle(fontSize = 14.sp), fontWeight = FontWeight.Bold
                    )
                }
            }
        }


        val authentication = get<Authentication>()
        val user by remember { mutableStateOf(authentication.currentUser()) }
        if (user != null) {
            if (isBookmarked) {
                Icon(
                    imageVector = Icons.Outlined.Bookmark,
                    contentDescription = "remove bookmark",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { removeBookmark(session.id) }
                        .padding(8.dp))
            } else {
                Icon(
                    imageVector = Icons.Outlined.BookmarkAdd,
                    contentDescription = "add bookmark",
                    modifier = Modifier
                        .clickable { addBookmark(session.id) }
                        .padding(8.dp))
            }
        }
    }
}
