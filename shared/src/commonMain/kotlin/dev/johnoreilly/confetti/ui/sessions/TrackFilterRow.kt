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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.GetConferenceDataQuery

@Composable
fun TrackFilterRow(
    tracks: List<GetConferenceDataQuery.Track>,
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
            val dotColor = track.color?.toColorOrNull()
            FilterChip(
                selected = selectedTrack == track.name,
                onClick = { onTrackSelected(if (selectedTrack == track.name) null else track.name) },
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

/** Parses a "0xAARRGGBB" string (as used for [dev.johnoreilly.confetti.GetConferenceDataQuery.Config.themeColor]) into a [Color], or null if absent/malformed. */
@OptIn(ExperimentalStdlibApi::class)
private fun String.toColorOrNull(): Color? {
    return try {
        Color(hexToLong(HexFormat { number.prefix = "0x" }))
    } catch (e: Exception) {
        null
    }
}
