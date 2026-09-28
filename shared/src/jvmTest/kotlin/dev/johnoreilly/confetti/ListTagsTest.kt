package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.preview.lightningSession
import dev.johnoreilly.confetti.preview.sessionDetails
import org.junit.Test
import kotlin.test.assertEquals

class ListTagsTest {
    private val tracks = setOf("droidCon", "swiftCon", "agentic codingCon")

    @Test
    fun tracksShownBeforeFormatAndLevel() {
        // Real next.app devCon Berlin 2026 tag list: format, level, then tracks (with repeats).
        val session = sessionDetails.copy(
            tags = listOf("Session", "Introductory and overview", "droidCon", "swiftCon", "swiftCon")
        )
        assertEquals(
            listOf("droidCon", "swiftCon", "Introductory and overview"),
            session.listTags(tracks),
        )
    }

    @Test
    fun selectedTrackShownFirst() {
        val session = sessionDetails.copy(
            tags = listOf("Keynote", "Intermediate", "droidCon", "agentic codingCon", "droidCon")
        )
        assertEquals(
            listOf("agentic codingCon", "droidCon", "Keynote"),
            session.listTags(tracks, selectedTrack = "agentic codingCon"),
        )
    }

    @Test
    fun lightningTagDroppedWhenBadgeShown() {
        val session = lightningSession.copy(tags = listOf("Lightning talk", "Intermediate", "swiftCon"))
        assertEquals(listOf("swiftCon", "Intermediate"), session.listTags(tracks))
    }

    @Test
    fun conferencesWithoutTracksKeepTagOrder() {
        assertEquals(listOf("Kotlin", "Multiplatform", "GraphQL"), sessionDetails.listTags())
    }
}
