package dev.johnoreilly.confetti.livery

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContractsTest {
    @Test fun noContractDeclaresThemeRoles() {
        // Theme roles (`theme.<role>`) are Livery's: the host fills them from its colour scheme and a
        // theme pack replaces them, without any contract listing them.
        AllContracts.forEach { c -> assertTrue(c.inputs.keys.none { it.startsWith("theme.") }, c.ref) }
    }

    @Test fun dataOnlyTakesTheContractsOwnInputs() {
        val data = SessionContract.data(mapOf(SessionContract.title to "Keynote", SessionContract.live to true))
        assertEquals(setOf("title", "live"), data.keys)
        // An input of another contract, even with the same name, is refused.
        assertFailsWith<IllegalArgumentException> { SessionContract.data(mapOf(SpeakerContract.name to "x")) }
    }

    @Test fun conferencesMapToASeries() {
        assertEquals("droidcon", ConferenceSeries.of("droidconlondon2025"))
        assertEquals("kotlinconf", ConferenceSeries.of("kotlinconf2026"))
        assertNull(ConferenceSeries.of("devfestnantes2025"))
    }

    @Test fun contractJsonDeclaresActionsWithTheirArgument() {
        val tickets = ConferenceContract.toJson()["actions"].toString()
        assertTrue("\"url\"" in tickets, tickets)
    }

    @Test fun contractJsonDeclaresListsAndWhenTheyCame() {
        val json = SessionContract.toJson()
        assertEquals(1, json["minor"].toString().toInt())
        val list = json["lists"].toString()
        assertTrue("\"speakerList\"" in list && "\"max\":4" in list && "\"since\":1" in list, list)
        val action = json["actions"].toString()
        assertTrue("\"openSpeaker\":{\"kind\":\"app:open-speaker\",\"args\":{\"id\"" in action, action)
        assertEquals("speakerList.2.name", SessionContract.speakerList.valueName(2, SessionContract.speakerName))
        assertFailsWith<IllegalArgumentException> { SessionContract.speakerList.valueName(4, SessionContract.speakerName) }
    }

    @Test fun listItemsTakeOnlyTheirOwnFields() {
        assertFailsWith<IllegalArgumentException> {
            SessionContract.data(mapOf(SessionContract.speakerList to listOf(mapOf(SessionContract.title to "x"))))
        }
        assertFailsWith<IllegalArgumentException> {
            SessionContract.data(mapOf(SessionContract.speakerList to List(5) { mapOf(SessionContract.speakerName to "n", SessionContract.speakerId to "i") }))
        }
    }

    @Test fun themePackNamesTheMode() {
        assertEquals("droidcon-dark", ConferenceSeries.themePack("droidcon", dark = true))
    }
}
