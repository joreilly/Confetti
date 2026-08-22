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
 * The color of this session's track, or null if it isn't tagged with any known track. A session
 * can carry multiple track tags - e.g. "Swift Export: Where We Stand" is tagged both droidCon and
 * swiftCon since it matters to both audiences, with source data
 * `["Session", "Introductory and overview", "droidCon", "swiftCon", "swiftCon"]`. Sessionize's
 * export consistently lists a session's actual/primary track *last* among its tags (matching
 * nextappcon.com's own agenda, which colors that exact session swiftCon, not droidCon) - so unlike
 * [TrackFilterRow], which just needs *a* match for filtering and doesn't care which, this takes the
 * last tag that names a known track rather than the first one in [tracks] (config) order.
 */
fun SessionDetails.trackColor(tracks: List<GetConferenceDataQuery.Track>): Color? {
    val byName = tracks.associateBy { it.name }
    return tags.lastOrNull { it in byName }?.let { byName.getValue(it) }?.color?.toColorOrNull()
}
