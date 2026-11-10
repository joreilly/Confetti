package dev.johnoreilly.confetti.livery

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The data classes fill their contract: a new input there fails here until the class maps it. */
class ContractDataTest {
    private val full: List<ContractData> = listOf(
        ConferenceCard("droidcon London", "29–30 OCT 2026", "London", "In person.", live = true, ticketsUrl = "https://london.droidcon.com"),
        SessionCard("Keynote", "Jane Doe", "Hall 1", "09:00 – 09:45", "android", live = true, bookmarked = true,
            speakerList = listOf(SessionSpeaker("s1", "Jane Doe"))),
        SpeakerCard("Jane Doe", "GDE · Acme", "https://example.com/jane.png", "2 sessions"),
        BreakCard("Coffee break", "Foyer", "10:30 – 11:00", coffee = true, live = true),
    )

    @Test fun everyInputIsMapped() {
        full.forEach { d ->
            val expected = (d.contract.inputs.keys + d.contract.lists.keys).toSet()
            assertEquals(expected, d.values().keys.map { it.name }.toSet(), d.contract.ref)
        }
        // Every app-filled contract has a data class.
        assertEquals(AllContracts.filter { it.dataSource == DataSource.App }.toSet(), full.map { it.contract }.toSet())
    }

    @Test fun valuesAreFittedNotRefused() {
        val long = SessionCard(title = "x".repeat(500)).toJson()
        assertEquals(SessionContract.title.maxLength, long["title"].toString().trim('"').length)
    }

    @Test fun speakersBindAsAListOfAtMostMax() {
        val many = (1..6).map { SessionSpeaker("id$it", "Speaker $it") }
        val json = SessionCard("Talk", speakerList = many + SessionSpeaker("x".repeat(65), "Too long an id")).toJson()
        val items = json["speakerList"] as kotlinx.serialization.json.JsonArray
        assertEquals(SessionContract.MAX_SPEAKERS, items.size)
        assertEquals("{\"name\":\"Speaker 1\",\"id\":\"id1\"}", items[0].toString())
        // No speakers, no list: an empty one would still bind, but there is nothing to draw.
        assertFalse("speakerList" in SessionCard("Talk").toJson())
    }

    @Test fun blankAndUnsafeValuesAreLeftOut() {
        val speaker = SpeakerCard("Jane", tagline = " ", photoUrl = "http://insecure.example/p.png").toJson()
        assertFalse("tagline" in speaker)
        assertFalse("photo" in speaker)
        // A URL is never cut: too long and it's left out.
        assertFalse("ticketsUrl" in ConferenceCard("x", "y", ticketsUrl = "https://" + "a".repeat(300)).toJson())
    }

    @Test fun theExperimentHasBothArms() {
        assertEquals(listOf(SduiExperiment.NATIVE, SduiExperiment.LIVERY), SduiExperiment.ARMS)
        assertTrue(Regex("[a-z][a-z0-9_]{0,19}").matches(SduiExperiment.ID))
    }
}
