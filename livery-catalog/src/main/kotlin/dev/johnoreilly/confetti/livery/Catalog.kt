package dev.johnoreilly.confetti.livery

import androidx.compose.runtime.Composable

/** One playable document of a design, for one Livery target. */
class TemplateDef(
    /** File stem: catalog/designs/<design>/<name>.rc and .json. */
    val name: String,
    /** Livery `Target` (PROTOCOL §3), as JSON. */
    val target: String,
    /** Capture size in dp. Layout happens in the player; this is the document's nominal size. */
    val widthDp: Int,
    val heightDp: Int,
    val content: @Composable () -> Unit,
)

class DesignDef(
    val id: String,
    val contract: ConfettiContract,
    val brand: ConferenceBrand,
    val title: String,
    /** Which of the contract's examples is this design's preview data. */
    val example: String,
    val templates: List<TemplateDef>,
    val install: String = "Always",
    val variant: String = "default",
)

private const val IN_APP = """{ "surface": "in-app", "rcApiLevel": 7, "profiles": ["androidx"], "size": { "minWidthDp": 280 } }"""
private const val LAUNCHER = """{ "surface": "launcher-widget", "rcApiLevel": 7, "profiles": ["widgets"], "size": { "minWidthDp": 110, "maxWidthDp": 400, "minHeightDp": 80, "maxHeightDp": 400 } }"""
private const val WEAR_LARGE = """{ "surface": "wear-widget", "rcApiLevel": 7, "profiles": ["glance-wear"], "container": "wear:large", "theme": "baked:dark" }"""

private fun inApp(heightDp: Int, content: @Composable () -> Unit) = TemplateDef("in-app", IN_APP, 360, heightDp, content)
private fun launcher(content: @Composable () -> Unit) = TemplateDef("launcher", LAUNCHER, 250, 120, content)
private fun wear(content: @Composable () -> Unit) = TemplateDef("wear-large", WEAR_LARGE, 200, 112, content)

/**
 * A brand's four designs: [sessionWidget] and [sessionWear] add the session's launcher and Wear
 * widget templates where the brand has them.
 */
fun designs(
    brand: ConferenceBrand, example: String, title: String,
    header: @Composable () -> Unit, session: @Composable () -> Unit, speaker: @Composable () -> Unit, promo: @Composable () -> Unit,
    // Capture heights: a slot is as tall as its document, so a design whose content needs more
    // room than the default must ask for it, or its last row (often the action) is clipped. Each
    // contract's `long` example renders every input at its limit to check that it fits.
    headerHeightDp: Int = 210,
    sessionHeightDp: Int = 150,
    speakerHeightDp: Int = 110,
    promoHeightDp: Int = 200,
    sessionWidget: (@Composable () -> Unit)? = null, sessionWear: (@Composable () -> Unit)? = null,
) = listOf(
    DesignDef("${brand.series}-header", ConferenceContract, brand, "$title header", example, listOf(inApp(headerHeightDp, header))),
    DesignDef("${brand.series}-session", SessionContract, brand, "$title session", example,
        listOfNotNull(inApp(sessionHeightDp, session), sessionWidget?.let(::launcher), sessionWear?.let(::wear))),
    DesignDef("${brand.series}-speaker", SpeakerContract, brand, "$title speaker", example, listOf(inApp(speakerHeightDp, speaker))),
    DesignDef("${brand.series}-promo", PromoContract, brand, "$title promotion", example, listOf(inApp(promoHeightDp, promo)), install = "OnDemand"),
)

/** Every design in every catalog. Each conference's designs join in their own PR. */
val AllDesigns: List<DesignDef> = ConfettiDesigns + DroidconDesigns + KotlinConfDesigns
