package dev.johnoreilly.confetti.ui

import androidx.compose.ui.graphics.Color
import dev.johnoreilly.confetti.GetConferenceDataQuery
import dev.johnoreilly.confetti.fragment.SessionDetails

/** Parses a "0xAARRGGBB" string (as used for [dev.johnoreilly.confetti.GetConferenceDataQuery.Track.color]
 *  and [dev.johnoreilly.confetti.GetConferenceDataQuery.Config.themeColor]) into a [Color], or null if
 *  absent/malformed. */
@OptIn(ExperimentalStdlibApi::class)
fun String?.toColorOrNull(): Color? {
    if (this == null) return null
    return try {
        Color(hexToLong(HexFormat { number.prefix = "0x" }))
    } catch (e: Exception) {
        null
    }
}

/**
 * The color of the first track (in [tracks] order) this session is tagged with, or null if it
 * isn't tagged with any known track. A session can carry multiple track tags (e.g. a cross-listed
 * session) - first match wins, same tie-break [TrackFilterRow] uses for its filter chips.
 */
fun SessionDetails.trackColor(tracks: List<GetConferenceDataQuery.Track>): Color? {
    return tracks.firstOrNull { it.name in tags }?.color?.toColorOrNull()
}
