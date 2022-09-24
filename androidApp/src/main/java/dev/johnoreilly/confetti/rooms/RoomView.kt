@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.rooms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.ui.component.ConfettiGradientBackground
import dev.johnoreilly.confetti.ui.component.ConfettiTopAppBar
import org.koin.androidx.compose.getViewModel


@Composable
fun RoomsRoute(viewModel: ConfettiViewModel = getViewModel()) {
    val rooms by viewModel.rooms.collectAsState(emptyList())
    RoomListView(rooms)
}


@Composable
fun RoomListView(rooms: List<RoomDetails>) {
    ConfettiGradientBackground {
        Scaffold(
            topBar = {
                ConfettiTopAppBar(
                    titleRes = R.string.rooms,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
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
