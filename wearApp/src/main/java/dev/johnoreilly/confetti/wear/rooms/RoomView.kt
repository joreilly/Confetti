package dev.johnoreilly.confetti.wear.rooms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.wear.ConfettiViewModel
import org.koin.androidx.compose.getViewModel


@Composable
fun RoomsRoute(viewModel: ConfettiViewModel = getViewModel()) {
    val rooms by viewModel.rooms.collectAsState(emptyList())
    RoomListView(rooms)
}


@Composable
fun RoomListView(rooms: List<RoomDetails>) {
        Column(modifier = Modifier) {
            if (rooms.isNotEmpty()) {
                LazyColumn {
                    items(rooms) { room ->
                        RoomView(room)
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
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
}
