@file:OptIn(ExperimentalFoundationApi::class)

package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.isLightning
import dev.johnoreilly.confetti.isService
import dev.johnoreilly.confetti.sessionSpeakers
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.SignInDialog
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.component.ConfettiTab
import dev.johnoreilly.confetti.ui.component.pagerTabIndicatorOffset
import kotlinx.coroutines.launch
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

                HorizontalPager(state = pagerState) { page ->
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
                                    ConfettiHeader(icon = Icons.Filled.AccessTime, text = startTime)
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


@Composable
fun SessionItemView(
    session: SessionDetails,
    sessionSelected: (sessionId: String) -> Unit,
    isBookmarked: Boolean,
    addBookmark: (String) -> Unit,
    removeBookmark: (String) -> Unit,
    onNavigateToSignIn: () -> Unit = {},
    isLoggedIn: Boolean,
) {

    var modifier = Modifier.fillMaxSize()
    if (!session.isBreak()) {
        modifier = modifier.clickable(onClick = {
            sessionSelected(session.id)
        })
    }

    if (session.isService()) {
        modifier = modifier.background(Color.White)
    }

    Row(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
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
    }
}
