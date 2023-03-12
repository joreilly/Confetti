@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.account.AccountIcon
import dev.johnoreilly.confetti.ui.component.ConfettiTab
import dev.johnoreilly.confetti.ui.component.pagerTabIndicatorOffset
import kotlinx.coroutines.launch


@Composable
fun SessionListView(
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConferenceSelected: () -> Unit,
    onRefresh: suspend (() -> Unit)
) {
    val refreshScope = rememberCoroutineScope()
    val pagerState = rememberPagerState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (uiState is SessionsUiState.Success) uiState.conferenceName else "") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    AccountIcon(
                        onSwitchConference = onSwitchConferenceSelected,
                        onSignIn = onSignIn,
                        onSignOut = onSignOut,
                    )
                }
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            when (uiState) {
                SessionsUiState.Loading ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        CircularProgressIndicator()
                    }

                is SessionsUiState.Success -> {
                    var refreshing by remember { mutableStateOf(false) }
                    fun refresh() {
                        refreshScope.launch {
                            refreshing = true
                            onRefresh()
                            refreshing = false
                        }
                    }

                    val state = rememberPullRefreshState(refreshing, ::refresh)

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
        uiState.confDates.forEachIndexed { index, date ->
            val coroutineScope = rememberCoroutineScope()

            ConfettiTab(
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = { Text(text = date.toString()) }
            )
        }
    }

}