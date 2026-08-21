package dev.johnoreilly.confetti.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun TrackFilterRow(
    tracks: List<String>,
    selectedTrack: String?,
    onTrackSelected: (String?) -> Unit,
) {
    if (tracks.isEmpty()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedTrack == null,
                onClick = { onTrackSelected(null) },
                label = { Text("All") },
            )
        }
        items(tracks) { track ->
            FilterChip(
                selected = selectedTrack == track,
                onClick = { onTrackSelected(if (selectedTrack == track) null else track) },
                label = { Text(track) },
            )
        }
    }
}
