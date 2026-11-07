package dev.johnoreilly.confetti.livery

import ee.schimke.composeai.rcplayer.protocol.RcDocumentCodec
import ee.schimke.composeai.rcplayer.protocol.RcShaderData
import ee.schimke.livery.client.DecisionContext
import ee.schimke.livery.client.TemplateQuery
import ee.schimke.livery.client.TemplateSelector
import ee.schimke.livery.client.designArm
import ee.schimke.livery.protocol.Manifest
import ee.schimke.livery.protocol.PlayerCapabilities
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * The shader experiment as Livery serves it: the KotlinConf channel the publisher builds
 * (`buildLiveryChannelKotlinconf`), read by Livery's own selector and experiment assignment.
 */
class ShaderExperimentTest {
    private val manifest: Manifest by lazy {
        val dir = File(requireNotNull(System.getProperty("livery.channels")) { "run through Gradle" }, "kotlinconf")
        Json { ignoreUnknownKeys = true }.decodeFromString(Manifest.serializer(), dir.resolve("manifest.json").readText())
    }

    private val noShaders = PlayerCapabilities("in-app", maxRcApiLevel = 7, profiles = setOf("androidx"))
    private val shaders = noShaders.copy(features = setOf("shaders"))

    private fun select(caps: PlayerCapabilities, designId: String? = null) = assertNotNull(
        TemplateSelector.select(
            manifest,
            TemplateQuery(ConferenceContract.ref, caps, widthDp = 360, designId = designId, tags = mapOf(ConferenceSeries.TAG to "kotlinconf")),
        ),
    )

    @Test fun theShaderTemplateIsMarkedAsNeedingShaders() {
        val templates = manifest.templates.filter { it.designId == "kotlinconf-header-shader" }
        assertEquals(listOf(listOf("shaders"), emptyList()), templates.map { it.features }.sortedByDescending { it.size })
    }

    @Test fun aPlayerThatRunsShadersGetsTheShader() {
        assertEquals(listOf("shaders"), select(shaders, "kotlinconf-header-shader").features)
    }

    @Test fun anyOtherPlayerGetsTheFallback() {
        val fallback = select(noShaders, "kotlinconf-header-shader")
        assertEquals(emptyList(), fallback.features)
        // The fallback is the default design's document, byte for byte.
        assertEquals(select(noShaders).sha256, fallback.sha256)
    }

    @Test fun outsideTheExperimentEveryoneGetsTheDefaultDesign() {
        assertEquals("kotlinconf-header", select(shaders).designId)
    }

    @Test fun theExperimentsArmsPickTheDesigns() = runBlocking {
        val context = DecisionContext(0L, "android", "en", 1L, "test", emptySet())
        for ((arm, design) in listOf("mesh" to "kotlinconf-header", "shader" to "kotlinconf-header-shader")) {
            val assigned = assertNotNull(designArm(manifest.experiments, ConferenceContract.ref, context) { arm })
            assertEquals(design, assigned.designId)
        }
    }

    @Test fun onlyTheShaderTemplateHasAShader() {
        val design = AllDesigns.single { it.id == "kotlinconf-header-shader" }
        val byTemplate = design.templates.associate { t ->
            t.name to RcDocumentCodec.decode(capture(design, t).bytes).operations.filterIsInstance<RcShaderData>().size
        }
        assertEquals(mapOf("in-app-shader" to 1, "in-app" to 0), byTemplate)
        assertTrue(MeshGradientAgsl.contains("uniform float time;"))
    }
}
