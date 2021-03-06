package dev.johnoreilly.confetti.rooms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.fragment.RoomDetails

@Composable
fun RoomListView(viewModel: ConfettiViewModel, bottomBar: @Composable () -> Unit) {
    val rooms by viewModel.rooms.collectAsState(emptyList())

    Scaffold(
        topBar = { TopAppBar (title = { Text("Rooms") } ) },
        bottomBar = bottomBar
    ) {
        if (rooms.isNotEmpty()) {
            LazyColumn {
                items(rooms) { room ->
                    RoomView(room)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
                CircularProgressIndicator()
            }
        }
    }
}


@Composable
fun RoomView(room: RoomDetails) {

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = {  })
        .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "${room.name} (${room.capacity})", style = TextStyle(fontSize = 16.sp))
    }

    Divider()
}
