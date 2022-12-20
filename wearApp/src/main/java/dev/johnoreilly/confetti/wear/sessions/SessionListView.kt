@file:OptIn(ExperimentalPagerApi::class)

package dev.johnoreilly.confetti.wear.sessions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import dev.johnoreilly.confetti.wear.SessionsUiState
import kotlinx.coroutines.launch

@Composable
fun SessionListView(
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    onSwitchConferenceSelected: () -> Unit,
    onRefresh: suspend (() -> Unit)
) {
    var showMenu by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState()

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
            val refreshScope = rememberCoroutineScope()
            var refreshing by remember { mutableStateOf(false) }
            fun refresh() {
                refreshScope.launch {
                    refreshing = true
                    onRefresh()
                    refreshing = false
                }
            }

            Column {
                HorizontalPager(
                    count = uiState.confDates.size,
                    state = pagerState,
                ) { page ->

                    val sessions = uiState.sessionsByStartTimeList[page]
                    Box(
                        Modifier
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
                                            color = MaterialTheme.colors.primary
                                        )
                                    }
                                }

                                items(it.value) { session ->
                                    SessionView(session, sessionSelected)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
