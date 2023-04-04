@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.SignInDialog

@Composable
fun SessionListGridView(
    uiState: SessionsUiState,
    sessionSelected: (SessionDetailsKey) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    user: User?,
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
                    pageCount = uiState.formattedConfDates.size,
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
                        val sessionInfoWidth = 280.dp

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
                                            conference = uiState.conference,
                                            sessionByTimeList = it,
                                            rooms = rooms,
                                            bookmarks = uiState.bookmarks,
                                            sessionInfoWidth = sessionInfoWidth,
                                            timeInfoWidth = timeInfoWidth,
                                            sessionSelected = sessionSelected,
                                            addBookmark = addBookmark,
                                            removeBookmark = removeBookmark,
                                            onNavigateToSignIn = onNavigateToSignIn,
                                            user = user,
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
    conference: String,
    sessionByTimeList: Map.Entry<String, List<SessionDetails>>,
    bookmarks: Set<String>,
    rooms: List<RoomDetails>,
    sessionInfoWidth: Dp,
    timeInfoWidth: Dp,
    sessionSelected: (SessionDetailsKey) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    user: User?
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
                    .height(220.dp)
                    .padding(bottom = 16.dp)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary)),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable(onClick = {
                                sessionSelected(SessionDetailsKey(conference, session.id))
                            }),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Start),
                            text = session.title,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        session.speakers.forEach { speaker ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                Bookmark(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    bookmarks = bookmarks,
                    session = session,
                    user = user,
                    removeBookmark = removeBookmark,
                    addBookmark = addBookmark,
                    onNavigateToSignIn = onNavigateToSignIn
                )
                }
            }
        }
    }
}

@Composable
private fun Bookmark(
    modifier: Modifier = Modifier,
    bookmarks: Set<String>,
    session: SessionDetails,
    user: User?,
    removeBookmark: (sessionId: String) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val isBookmarked = bookmarks.contains(session.id)
    if (isBookmarked) {
        IconButton(
            modifier = modifier,
            onClick = {
                if (user != null) {
                    removeBookmark(session.id)
                } else {
                    showDialog = true
                }
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.Bookmark,
                contentDescription = "remove bookmark",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
    } else {
        IconButton(
            modifier = modifier,
            onClick = {
                if (user != null) {
                    addBookmark(session.id)
                } else {
                    showDialog = true
                }
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.BookmarkAdd,
                contentDescription = "add bookmark",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
    if (showDialog) {
        SignInDialog(
            onDismissRequest = { showDialog = false },
            onSignInClicked = onNavigateToSignIn
        )
    }
}
