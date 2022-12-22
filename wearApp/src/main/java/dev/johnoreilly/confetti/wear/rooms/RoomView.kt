@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.rooms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.fragment.RoomDetails
import dev.johnoreilly.confetti.wear.ConfettiViewModel
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import dev.johnoreilly.confetti.wear.ui.previews.WearSmallRoundDevicePreview
import org.koin.androidx.compose.getViewModel


@Composable
fun RoomsRoute(
    columnState: ScalingLazyColumnState,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val rooms by viewModel.rooms.collectAsState(null)
    RoomListView(rooms, columnState)
}


@Composable
fun RoomListView(
    rooms: List<RoomDetails>?,
    columnState: ScalingLazyColumnState
) {
    if (rooms != null) {
        ScalingLazyColumn(columnState = columnState) {
            item {
                ListHeader {
                    Text("Rooms")
                }
            }
            items(rooms) { room ->
                RoomView(room)
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun RoomView(room: RoomDetails) {
    Chip(
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(text = "${room.name} (${room.capacity})")
        },
        onClick = {}
    )
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun RoomListViewPreview() {
    ConfettiTheme {
        RoomListView(
            rooms = listOf(
                RoomDetails("1", "Main Hall", 200),
                RoomDetails("2", "The one with through the lobby", 10),
                RoomDetails("3", "Spillover", 50),
            ),
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}

@WearSmallRoundDevicePreview
@Composable
fun RoomListViewLoadingPreview() {
    ConfettiTheme {
        RoomListView(
            rooms = null,
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}
