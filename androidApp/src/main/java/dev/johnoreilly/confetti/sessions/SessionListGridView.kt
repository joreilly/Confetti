@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.fragment.SessionDetails

@Composable
fun SessionListGridView(
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    onSwitchConferenceSelected: () -> Unit,
    onRefresh: suspend (() -> Unit)
) {
    var showMenu by remember { mutableStateOf(false) }

    if (uiState is SessionsUiState.Success) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(uiState.conferenceName, fontSize = 40.sp) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Switch Conference") },
                                onClick = {
                                    showMenu = false
                                    onSwitchConferenceSelected()
                                }
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            BoxWithConstraints(Modifier.padding(padding)) {

                val rooms = uiState.rooms
                // TODO add tab bar for different dates (or show all days in same grid?)
                val sessionsByStartTime = uiState.sessionsByStartTimeList[0]
                val timeInfoWidth = 80.dp
                val sessionInfoWidth = (maxWidth - timeInfoWidth - 16.dp) / rooms.size


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

                    LazyColumn(modifier = Modifier.fillMaxWidth().padding(end = 16.dp)) {
                        sessionsByStartTime.forEach {
                            item {
                                SessionGridRow(it, rooms, sessionInfoWidth, timeInfoWidth, sessionSelected)
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
            modifier = Modifier.width(timeInfoWidth).padding(16.dp),
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
                    modifier = Modifier.padding(16.dp).clickable(onClick = {
                        sessionSelected(session.id)
                    })
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = session.title,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(16.dp))

                    session.speakers.forEach { speaker ->
                        Text(
                            text = speaker.speakerDetails.name,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                }
            }
        }
    }
}
