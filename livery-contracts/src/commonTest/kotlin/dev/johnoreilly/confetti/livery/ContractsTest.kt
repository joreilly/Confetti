package dev.johnoreilly.confetti.livery

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContractsTest {
    @Test fun everyContractTakesTheThemeRoles() {
        AllContracts.forEach { c ->
            assertEquals(
                listOf("primary", "onPrimary", "secondary", "tertiary", "surface", "onSurface", "onSurfaceVariant"),
                c.inputs.values.mapNotNull { it.role }, c.ref,
            )
        }
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

    @Test fun materialColoursCoverEveryThemeRole() {
        val values = materialThemeValues(1, 2, 3, 4, 5, 6, 7)
        AllContracts.forEach { c ->
            assertEquals(c.inputs.values.filter { it.role != null }.map { it.name }.toSet(), values.keys, c.ref)
        }
        assertEquals("droidcon-dark", ConferenceSeries.themePack("droidcon", dark = true))
    }
}
