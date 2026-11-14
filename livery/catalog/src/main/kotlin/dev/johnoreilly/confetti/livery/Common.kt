package dev.johnoreilly.confetti.livery

import ee.schimke.composeai.rccreation.compose.text.RemoteTextOverflow
import ee.schimke.composeai.rccreation.compose.text.RemoteFontWeight
import ee.schimke.livery.protocol.ThemeRoles
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteRow
import ee.schimke.composeai.rccreation.compose.layout.RemoteOffset
import ee.schimke.composeai.rccreation.compose.shaders.RemoteBrush
import ee.schimke.composeai.rccreation.compose.shaders.radialGradient
import ee.schimke.composeai.rccreation.compose.state.cos
import ee.schimke.composeai.rccreation.compose.layout.RemoteText
import ee.schimke.composeai.rccreation.compose.state.RemoteTimeVariables
import ee.schimke.composeai.rccreation.compose.state.rs
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.alpha
import ee.schimke.composeai.rccreation.compose.modifier.background
import ee.schimke.composeai.rccreation.compose.modifier.clickable
import ee.schimke.composeai.rccreation.compose.modifier.clip
import ee.schimke.composeai.rccreation.compose.modifier.padding
import ee.schimke.composeai.rccreation.compose.modifier.visibility
import ee.schimke.composeai.rccreation.compose.shapes.RemoteRoundedCornerShape
import ee.schimke.composeai.rccreation.compose.state.RemoteBoolean
import ee.schimke.composeai.rccreation.compose.state.RemoteColor
import ee.schimke.composeai.rccreation.compose.state.RemoteDp
import ee.schimke.composeai.rccreation.compose.state.RemoteFloat
import ee.schimke.composeai.rccreation.compose.state.RemoteInt
import ee.schimke.composeai.rccreation.compose.state.RemoteString
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.rf
import ee.schimke.composeai.rccreation.compose.state.ri
import ee.schimke.composeai.rccreation.compose.state.rsp
import ee.schimke.composeai.rccreation.compose.state.sin
import ee.schimke.composeai.rccreation.compose.text.RemoteFontFamily
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/** The brand as named values: what a theme pack overrides, with the brand as initial values. */
class BrandColors(
    val primary: RemoteColor,
    val onPrimary: RemoteColor,
    val secondary: RemoteColor,
    val tertiary: RemoteColor,
    val surface: RemoteColor,
    val onSurface: RemoteColor,
    val muted: RemoteColor,
)

@Composable
fun brandColors(brand: ConferenceBrand): BrandColors = BrandColors(
    primary = themeColor(ThemeRoles.PRIMARY, brand.primary),
    onPrimary = themeColor(ThemeRoles.ON_PRIMARY, brand.onPrimary),
    secondary = themeColor(ThemeRoles.SECONDARY, brand.secondary),
    tertiary = themeColor(ThemeRoles.TERTIARY, brand.tertiary),
    surface = themeColor(ThemeRoles.SURFACE, brand.surface),
    onSurface = themeColor(ThemeRoles.ON_SURFACE, brand.onSurface),
    muted = themeColor(ThemeRoles.ON_SURFACE_VARIANT, brand.muted),
)

// Remote Compose's Component.Visibility values.
private const val GONE = 0
private const val VISIBLE = 1

fun RemoteBoolean.visibleIf(): RemoteInt = select(VISIBLE.ri, GONE.ri)

/**
 * One soft light in a [meshGlows] background: a radial glow of [color] fading to clear, centred
 * at ([x], [y]) dp and orbiting that point by [drift] dp once every [periodSeconds].
 */
class Glow(
    val color: RemoteColor,
    val x: Float,
    val y: Float,
    val radius: Float,
    val drift: Float = 40f,
    val periodSeconds: Float = 30f,
    val phase: Float = 0f,
)

/**
 * A mesh-gradient look without a custom shader (Remote Compose has none, only linear, radial and
 * sweep gradients): radial glows layered over the card's own background, each on a slow ellipse
 * on the document's animation clock. Meant to be barely noticed: low-alpha colours and periods of 20–40 s,
 * so it reads as a living surface, not an animation. Costs nothing on the app side.
 */
fun RemoteModifier.meshGlows(glows: List<Glow>): RemoteModifier = glows.fold(this) { m, g ->
    val t = RemoteTimeVariables.animationTime * (2f * Math.PI.toFloat() / g.periodSeconds) + g.phase
    m.background(
        RemoteBrush.radialGradient(
            colors = listOf(g.color, g.color.copy(alpha = 0f.rf)),
            // In dp: capture uses the host's density (GenerateCatalog), so toPx() is an expression the
            // player evaluates, and the mesh sits the same on every screen.
            center = RemoteOffset(px(cos(t) * g.drift + g.x), px(sin(t) * (g.drift * 0.6f) + g.y)),
            radius = px(g.radius.rf),
        ),
    )
}

private fun px(dp: RemoteFloat): RemoteFloat = RemoteDp(dp).toPx()

/** 0.45…1.0, once every ~1.5 s, on the document's animation clock: no recomposition, no re-capture. */
fun pulse(): RemoteFloat = sin(RemoteTimeVariables.animationTime * 4f) * 0.275f + 0.725f

@Composable
fun Label(
    text: RemoteString,
    color: RemoteColor,
    size: Int = 12,
    weight: FontWeight = FontWeight.Bold,
    modifier: RemoteModifier = RemoteModifier,
    maxLines: Int = 1,
) {
    val family = LocalTemplateScope.current.font()
    RemoteText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.rsp,
        fontWeight = RemoteFontWeight(weight.weight),
        fontFamily = family?.let { RemoteFontFamily.Named(it.removePrefix("system:")) } ?: RemoteFontFamily.Default,
        overflow = RemoteTextOverflow.Ellipsis,
        maxLines = maxLines,
    )
}

/**
 * A session's speakers, each its own tap target: `openSpeaker` with that speaker's `id`, so tapping
 * one of two speakers opens that one (the card's own tap still opens the session). Unrolled up to
 * the contract's `speakerList.max`, each name shown while its index is below the bound count, with
 * ", " between them. A row too narrow for every name ellipsizes the last one it reaches.
 */
@Composable
fun SpeakerLinks(color: RemoteColor, size: Int = 13, weight: FontWeight = FontWeight.Normal, modifier: RemoteModifier = RemoteModifier) {
    val list = SessionContract.speakerList
    val count = listCount(list)
    RemoteRow(modifier = modifier, verticalAlignment = RemoteAlignment.CenterVertically) {
        for (i in 0 until list.max) {
            val shown = RemoteModifier.visibility(count.isGreaterThan(i.ri).visibleIf())
            // A trailing space is trimmed from a text, a no-break space isn't: the gap after "," is
            // then the font's own space, as wide in a monospaced brand font as in any other.
            if (i > 0) Label(",\u00A0".rs, color, size = size, weight = weight, modifier = shown)
            val id = listText(list, i, SessionContract.speakerId)
            Label(
                listText(list, i, SessionContract.speakerName), color, size = size, weight = weight,
                modifier = shown.clickable(emit(SessionContract.openSpeaker, id)),
            )
        }
    }
}

/** A break's glyph: ☕ for a break between talks, 🎉 for a party, ⓘ for anything else (registration). */
@Composable
fun BreakIcon(color: RemoteColor, size: Int = 18) {
    val c = BreakContract
    Label(inputBool(c.coffee).select("☕".rs, inputBool(c.social).select("🎉".rs, "ⓘ".rs)), color, size = size, weight = FontWeight.Normal)
}

/** A pill that only shows while [live] is true, breathing on the player's clock. */
@Composable
fun LivePill(live: RemoteBoolean, background: RemoteColor, content: RemoteColor, text: String = "LIVE") {
    RemoteBox(
        modifier = RemoteModifier
            .visibility(live.visibleIf())
            .alpha(pulse())
            .clip(RemoteRoundedCornerShape(50.rdp))
            .background(background)
            .padding(horizontal = 8.rdp, vertical = 2.rdp),
    ) {
        Label(text.rs, content, size = 11)
    }
}

/** A small rounded tag. */
@Composable
fun Chip(
    text: RemoteString,
    background: RemoteColor,
    content: RemoteColor,
    corner: Int = 50,
    modifier: RemoteModifier = RemoteModifier,
) {
    RemoteBox(
        modifier = modifier
            .clip(RemoteRoundedCornerShape(corner.rdp))
            .background(background)
            .padding(horizontal = 10.rdp, vertical = 4.rdp),
    ) {
        Label(text, content, size = 12, weight = FontWeight.SemiBold)
    }
}
