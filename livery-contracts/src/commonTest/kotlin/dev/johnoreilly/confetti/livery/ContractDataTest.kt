package dev.johnoreilly.confetti.livery

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The data classes fill their contract: a new input there fails here until the class maps it. */
class ContractDataTest {
    private val full: List<ContractData> = listOf(
        ConferenceCard("droidcon London", "29–30 OCT 2026", "London", "In person.", live = true, ticketsUrl = "https://london.droidcon.com"),
        SessionCard("Keynote", "Jane Doe", "Hall 1", "09:00 – 09:45", "android", live = true, bookmarked = true),
        SpeakerCard("Jane Doe", "GDE · Acme", "https://example.com/jane.png", "2 sessions"),
    )

    @Test fun everyNonThemeInputIsMapped() {
        full.forEach { d ->
            val expected = d.contract.inputs.values.filter { it.role == null }.map { it.name }.toSet()
            assertEquals(expected, d.values().keys.map { it.name }.toSet(), d.contract.ref)
        }
        // Every app-filled contract has a data class.
        assertEquals(AllContracts.filter { it.dataSource == DataSource.App }.toSet(), full.map { it.contract }.toSet())
    }

    @Test fun valuesAreFittedNotRefused() {
        val long = SessionCard(title = "x".repeat(500)).toJson()
        assertEquals(SessionContract.title.maxLength, long["title"].toString().trim('"').length)
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
