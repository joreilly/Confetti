@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView

@Composable
fun SessionListGridView(
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    onRefresh: () -> Unit,
) {
    val pagerState = rememberPagerState()

    when (uiState) {
        SessionsUiState.Error -> ErrorView(onRefresh)
        SessionsUiState.Loading -> LoadingView()

        is SessionsUiState.Success -> {

            Column {

                SessionListTabRow(pagerState, uiState)

                HorizontalPager(
                    pageCount = uiState.confDates.size,
                    state = pagerState,
                ) { page ->

                    Row(
                        Modifier
                            .padding(bottom = 60.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        val sessionsByStartTime = uiState.sessionsByStartTimeList[page]

                        val rooms = uiState.rooms.filter { room ->
                            sessionsByStartTime.values.any { it.any { it.room?.name == room.name } }
                        }

                        val timeInfoWidth = 90.dp
                        val sessionInfoWidth = 240.dp


                        Column {
                            Row(
                                modifier = Modifier.padding(
                                    start = timeInfoWidth,
                                    top = 16.dp,
                                    bottom = 16.dp
                                )
                            ) {
                                rooms.forEach { room ->
                                    Text(
                                        modifier = Modifier.width(sessionInfoWidth),
                                        textAlign = TextAlign.Center,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        text = room.name
                                    )
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp)
                            ) {
                                sessionsByStartTime.forEach {
                                    item {
                                        SessionGridRow(
                                            it,
                                            rooms,
                                            sessionInfoWidth,
                                            timeInfoWidth,
                                            sessionSelected
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
}


@Composable
fun SessionGridRow(
    sessionByTimeList: Map.Entry<String, List<SessionDetails>>,
    rooms: List<RoomDetails>,
    sessionInfoWidth: Dp,
    timeInfoWidth: Dp,
    sessionSelected: (sessionId: String) -> Unit
) {
    Row {
        Text(
            sessionByTimeList.key,
            modifier = Modifier
                .width(timeInfoWidth)
                .padding(16.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )

        val sessionList = rooms.mapNotNull { room ->
            sessionByTimeList.value.find { it.room?.name == room.name }
        }

        sessionList.forEach { session ->
            Surface(
                modifier = Modifier
                    .width(sessionInfoWidth)
                    .height(180.dp)
                    .padding(bottom = 16.dp)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary)),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable(onClick = {
                            sessionSelected(session.id)
                        }),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = session.title,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(16.dp))

                    Spacer(modifier = Modifier.weight(1f))
                    session.speakers.forEach { speaker ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {
                            if (speaker.speakerDetails.photoUrl?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = speaker.speakerDetails.photoUrl,
                                    contentDescription = speaker.speakerDetails.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = speaker.speakerDetails.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                }
            }
        }
    }
}
