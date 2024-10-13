package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
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
import coil3.compose.AsyncImage
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isLightning
import dev.johnoreilly.confetti.isService
import dev.johnoreilly.confetti.ui.icons.Bolt
import dev.johnoreilly.confetti.ui.icons.Bookmark
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons

@Composable
fun SessionListGridView(
    uiState: SessionsUiState,
    sessionSelected: (id: String) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    isLoggedIn: Boolean,
    onRefresh: () -> Unit,
) {
    when (uiState) {
        SessionsUiState.Error -> ErrorView(onRefresh)
        SessionsUiState.Loading -> LoadingView()

        is SessionsUiState.Success -> {

            println("SessionsUiState.Success, conference = ${uiState.conferenceName}")
            Column {
                val pagerState = rememberPagerState {
                    uiState.formattedConfDates.size
                }

                SessionListTabRow(pagerState, uiState)

                HorizontalPager(state = pagerState) { page ->

                    Row(Modifier.horizontalScroll(rememberScrollState())) {                        val sessionsByStartTime = uiState.sessionsByStartTimeList[page]

                        val rooms = uiState.rooms.filter { room ->
                            sessionsByStartTime.values.any { session -> session.any { it.room?.name == room.name } }
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
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(end = 16.dp)
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
}


@Composable
fun SessionGridRow(
    conference: String,
    sessionByTimeList: Map.Entry<String, List<SessionDetails>>,
    bookmarks: Set<String>,
    rooms: List<RoomDetails>,
    sessionInfoWidth: Dp,
    timeInfoWidth: Dp,
    sessionSelected: (id: String) -> Unit,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    isLoggedIn: Boolean,
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

        val height = if (sessionList.size == 1 && sessionList[0].isService())
            100.dp
        else
            220.dp

        rooms.forEach { room ->
            val session = sessionList.firstOrNull { it.room?.name == room.name }

            if (session != null) {
                Surface(
                    modifier = Modifier
                        .width(sessionInfoWidth)
                        .height(height)
                        .padding(bottom = 16.dp)
                        .clickable(onClick = {
                            sessionSelected(session.id)
                        })
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary)),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.Start),
                                text = session.title,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            //Spacer(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Speakers(conference, session)
                            if (session.isLightning()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Row(Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                                        // TODO find alternative
                                        Icon(imageVector = ConfettiIcons.Bolt, contentDescription = "lightning")
                                        Spacer(Modifier.width(4.dp))
                                        Text("Lightning / ${session.startsAt.time}-${session.endsAt.time}")
                                    }
                                }
                            }
                        }
                        Bookmark(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            bookmarks = bookmarks,
                            session = session,
                            isLoggedIn = isLoggedIn,
                            removeBookmark = removeBookmark,
                            addBookmark = addBookmark,
                            onNavigateToSignIn = onNavigateToSignIn
                        )
                    }
                }
            } else {
                Box(Modifier
                    .width(sessionInfoWidth)
                    .height(height)
                )
            }
        }
    }
}

@Composable
private fun Speakers(conference: String, session: SessionDetails) {
    session.speakers.forEach { speaker ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (speaker.speakerDetails.photoUrl?.isNotEmpty() == true) {
                val url = "https://confetti-app.dev/images/avatar/${conference}/${speaker.id}"
                AsyncImage(
                    model = url,
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

@Composable
private fun Bookmark(
    modifier: Modifier = Modifier,
    bookmarks: Set<String>,
    session: SessionDetails,
    isLoggedIn: Boolean,
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
                if (isLoggedIn) {
                    removeBookmark(session.id)
                } else {
                    showDialog = true
                }
            }
        ) {
            Icon(
                imageVector = ConfettiIcons.Bookmark,
                contentDescription = "remove bookmark",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
    } else {
        // disable from this view for now
/*
        IconButton(
            modifier = modifier,
            onClick = {
                if (isLoggedIn) {
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

 */
    }
    if (showDialog) {
        SignInDialog(
            onDismissRequest = { showDialog = false },
            onSignInClicked = onNavigateToSignIn
        )
    }
}
