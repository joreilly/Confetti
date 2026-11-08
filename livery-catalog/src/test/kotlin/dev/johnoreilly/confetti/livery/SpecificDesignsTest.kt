package dev.johnoreilly.confetti.livery

import ee.schimke.livery.client.TemplateQuery
import ee.schimke.livery.client.TemplateSelector
import ee.schimke.livery.protocol.Manifest
import ee.schimke.livery.protocol.PlayerCapabilities
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

/**
 * Designs made for one kind of session, as the app asks for them: tag sets most specific first
 * (`session`, each `tag`, `type`), then the series' own, and the first that has a template draws.
 */
class SpecificDesignsTest {
    private val manifest: Manifest by lazy {
        val dir = File(requireNotNull(System.getProperty("livery.channels")) { "run through Gradle" }, "kotlinconf")
        Json { ignoreUnknownKeys = true }.decodeFromString(Manifest.serializer(), dir.resolve("manifest.json").readText())
    }

    private val inApp = PlayerCapabilities("in-app", maxRcApiLevel = 7, profiles = setOf("androidx"))

    private fun query(tags: Map<String, String> = emptyMap()) =
        TemplateQuery(SessionContract.ref, inApp, widthDp = 360, tags = mapOf(ConferenceSeries.TAG to "kotlinconf") + tags)

    /** The design a session card draws: the app's chain, through Livery's selector. */
    private fun design(id: String, tags: List<String>, type: String): String? {
        val specific = listOf(mapOf("session" to id)) + tags.map { mapOf("tag" to it) } + listOf(mapOf("type" to type))
        return (specific.map(::query) + query()).firstNotNullOfOrNull { TemplateSelector.select(manifest, it) }?.designId
    }

    @Test fun theKeynoteDrawsTheDesignMadeForIt() {
        // Before its tags: the talk itself is the most specific.
        assertEquals("kotlinconf-session-keynote", design(KOTLINCONF_2026_KEYNOTE, listOf("Regular session", "Lightning talk"), "talk"))
    }

    @Test fun aLightningTalkDrawsTheDesignForItsTag() {
        assertEquals("kotlinconf-session-lightning", design("t2", listOf("Intermediate", "Lightning talk", "Android"), "talk"))
    }

    @Test fun everyOtherSessionDrawsTheSeriesDesign() {
        assertEquals("kotlinconf-session", design("t1", listOf("Regular session", "Android"), "talk"))
    }

    @Test fun thePlainQueryNeverPicksATaggedDesign() {
        // Their own variants keep them out: the default design wins the plain query.
        assertEquals("kotlinconf-session", TemplateSelector.select(manifest, query())?.designId)
    }

    @Test fun aTagNoDesignIsMadeForHasNoTemplate() {
        assertNull(TemplateSelector.select(manifest, query(mapOf("tag" to "Workshop"))))
    }
}
