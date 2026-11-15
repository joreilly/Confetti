package dev.johnoreilly.confetti.livery

import androidx.compose.runtime.Composable
import ee.schimke.composeai.rccreation.common.Profile
import ee.schimke.composeai.rccreation.common.RcPlatformProfiles
import ee.schimke.composeai.rcwidget.creation.RcCaptureProfiles

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
) {
    /** A launcher's or a watch's widget: an API level 7 player, drawing a document with its theme baked in. */
    val isWidget: Boolean get() = "\"launcher-widget\"" in target || isWearWidget

    /** A Glance Wear widget: its host loads no fonts, so its labels are the platform's. */
    val isWearWidget: Boolean get() = "\"wear-widget\"" in target

    /** What the capture writes: the target's player's allow-list (Glance Wear's, a launcher's) or the app's. */
    val captureProfile: Profile get() = when {
        "\"glance-wear\"" in target -> RcCaptureProfiles.wearWidgets
        "\"widgets\"" in target -> RcCaptureProfiles.launcherWidgets
        else -> RcPlatformProfiles.ANDROIDX
    }
}

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
    /**
     * Beyond its series: what this design is for, e.g. `type=keynote`, `tag=<a track or category>`
     * or `session=<id>`. The app asks for those most specific first, and falls back to the
     * series' default design. A tagged design needs a [variant] of its own, so the plain query's
     * `default` still wins for every other item.
     */
    val tags: Map<String, String> = emptyMap(),
)

internal const val IN_APP = """{ "surface": "in-app", "rcApiLevel": 7, "profiles": ["androidx"], "size": { "minWidthDp": 280 } }"""
private const val LAUNCHER = """{ "surface": "launcher-widget", "rcApiLevel": 7, "profiles": ["widgets"], "size": { "minWidthDp": 110, "maxWidthDp": 400, "minHeightDp": 80, "maxHeightDp": 400 } }"""
private const val WEAR_SMALL = """{ "surface": "wear-widget", "rcApiLevel": 7, "profiles": ["glance-wear"], "container": "wear:small", "theme": "baked:dark" }"""
private const val WEAR_LARGE = """{ "surface": "wear-widget", "rcApiLevel": 7, "profiles": ["glance-wear"], "container": "wear:large", "theme": "baked:dark" }"""

/** The top bar's title: in the app, narrower than a card (between the bar's buttons). */
internal const val IN_APP_BAR = """{ "surface": "in-app", "rcApiLevel": 7, "profiles": ["androidx"], "size": { "minWidthDp": 120 } }"""

private fun inApp(heightDp: Int, content: @Composable () -> Unit) = TemplateDef("in-app", IN_APP, 360, heightDp, content = content)
private fun launcher(content: @Composable () -> Unit) = TemplateDef("launcher", LAUNCHER, 250, 120, content = content)
/**
 * A Wear widget's two containers, captured at Glance Wear's default round-watch frames: the watch
 * writes its own container's padding, corners and fill in when it draws one (RcWearTemplateContainer).
 */
private fun wear(content: @Composable (WearWidgetSize) -> Unit) = listOf(
    TemplateDef("wear-small", WEAR_SMALL, 200, 60) { content(WearWidgetSize.Small) },
    TemplateDef("wear-large", WEAR_LARGE, 200, 112) { content(WearWidgetSize.Large) },
)

/** Which of a Wear widget's containers a template is for. */
enum class WearWidgetSize { Small, Large }

/**
 * A brand's four designs: [sessionWidget] and [sessionWear] add the session's launcher and Wear
 * widget templates (a Wear widget's small and large containers) where the brand has them.
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
    sessionWidget: (@Composable () -> Unit)? = null, sessionWear: (@Composable (WearWidgetSize) -> Unit)? = null,
    /** An agenda break (`confetti/break@1`); without one, the app draws Confetti's own row. */
    breakRow: (@Composable () -> Unit)? = null,
    breakHeightDp: Int = 76,
    /** The top bar's title (`confetti/title@1`); without one, the app draws Material's. */
    topBarTitle: (@Composable () -> Unit)? = null,
    /** A speaker page's header (`confetti/speaker-header@1`); without one, the app draws Confetti's own. */
    speakerHeader: (@Composable () -> Unit)? = null,
    speakerHeaderHeightDp: Int = 340,
    /** A session page's header (`confetti/session-header@1`); without one, the app draws Confetti's own. */
    sessionHeader: (@Composable () -> Unit)? = null,
    sessionHeaderHeightDp: Int = 190,
    /** A list's or page's section header (`confetti/section@1`); without one, the app draws Confetti's own. */
    section: (@Composable () -> Unit)? = null,
    sectionHeightDp: Int = 40,
) = listOfNotNull(
    DesignDef("${brand.series}-header", ConferenceContract, brand, "$title header", example, listOf(inApp(headerHeightDp, header))),
    DesignDef("${brand.series}-session", SessionContract, brand, "$title session", example,
        listOfNotNull(inApp(sessionHeightDp, session), sessionWidget?.let(::launcher)) + sessionWear?.let(::wear).orEmpty()),
    DesignDef("${brand.series}-speaker", SpeakerContract, brand, "$title speaker", example, listOf(inApp(speakerHeightDp, speaker))),
    DesignDef("${brand.series}-promo", PromoContract, brand, "$title promotion", example, listOf(inApp(promoHeightDp, promo)), install = "OnDemand"),
    breakRow?.let { DesignDef("${brand.series}-break", BreakContract, brand, "$title break", example, listOf(inApp(breakHeightDp, it))) },
    topBarTitle?.let { DesignDef("${brand.series}-title", TitleContract, brand, "$title title", example, listOf(TemplateDef("in-app", IN_APP_BAR, 280, 48, content = it))) },
    speakerHeader?.let { DesignDef("${brand.series}-speaker-header", SpeakerHeaderContract, brand, "$title speaker page", example, listOf(inApp(speakerHeaderHeightDp, it))) },
    sessionHeader?.let { DesignDef("${brand.series}-session-header", SessionHeaderContract, brand, "$title session page", example, listOf(inApp(sessionHeaderHeightDp, it))) },
    section?.let { DesignDef("${brand.series}-section", SectionContract, brand, "$title section header", example, listOf(inApp(sectionHeightDp, it))) },
)

/** Every design in every catalog. Each conference's designs join in their own PR. */
val AllDesigns: List<DesignDef> = ConfettiDesigns + DroidconDesigns + KotlinConfDesigns + KotlinConfShaderDesigns + KotlinConfKeynoteDesigns
