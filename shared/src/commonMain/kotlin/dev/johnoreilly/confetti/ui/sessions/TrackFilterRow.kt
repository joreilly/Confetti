package dev.johnoreilly.confetti.ui.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.GetConferenceDataQuery
import dev.johnoreilly.confetti.ui.toColorOrNull

@Composable
fun TrackFilterRow(
    tracks: List<GetConferenceDataQuery.Track>,
    selectedTracks: Set<String>,
    onTrackSelectionChanged: (Set<String>) -> Unit,
) {
    if (tracks.isEmpty()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedTracks.isEmpty(),
                onClick = { onTrackSelectionChanged(emptySet()) },
                label = { Text("All") },
            )
        }
        items(tracks) { track ->
            val dotColor = track.color?.toColorOrNull()
            FilterChip(
                selected = track.name in selectedTracks,
                onClick = {
                    onTrackSelectionChanged(selectedTracks.toggleTrack(track.name, tracks.map { it.name }))
                },
                label = { Text(track.name) },
                leadingIcon = dotColor?.let {
                    {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(it)
                        )
                    }
                },
            )
        }
    }
}

/**
 * Tracks are cumulative (e.g. droidCon + swiftCon), so tapping one adds or removes it rather than
 * replacing the previous choice. Selecting every track is the same as no filter, so that collapses
 * back to empty to show as "All".
 */
fun Set<String>.toggleTrack(track: String, allTracks: Collection<String>): Set<String> {
    val newSelection = if (track in this) this - track else this + track
    return if (allTracks.all { it in newSelection }) emptySet() else newSelection
}
