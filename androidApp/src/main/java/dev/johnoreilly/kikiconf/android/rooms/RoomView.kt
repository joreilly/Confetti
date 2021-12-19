package dev.johnoreilly.kikiconf.android.rooms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import dev.johnoreilly.kikiconf.android.KikiConfViewModel
import dev.johnoreilly.kikiconf.model.Room

@Composable
fun RoomListView(viewModel: KikiConfViewModel) {
    val rooms by viewModel.rooms.collectAsState()

    LazyColumn {
        items(rooms) { room ->
            RoomView(room)
        }
    }
}


@Composable
fun RoomView(room: Room) {

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = {  })
        .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = room.name, style = TextStyle(fontSize = 20.sp))
    }

    Divider()
}
