package dev.johnoreilly.confetti.livery

import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteOffset
import androidx.compose.remote.creation.compose.shaders.RemoteBrush
import androidx.compose.remote.creation.compose.shaders.radialGradient
import androidx.compose.remote.creation.compose.state.cos
import androidx.compose.remote.creation.compose.layout.RemoteText
import androidx.compose.remote.creation.compose.state.RemoteTimeVariables
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.alpha
import androidx.compose.remote.creation.compose.modifier.background
import androidx.compose.remote.creation.compose.modifier.clip
import androidx.compose.remote.creation.compose.modifier.padding
import androidx.compose.remote.creation.compose.modifier.visibility
import androidx.compose.remote.creation.compose.shapes.RemoteRoundedCornerShape
import androidx.compose.remote.creation.compose.state.RemoteBoolean
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteDp
import androidx.compose.remote.creation.compose.state.RemoteFloat
import androidx.compose.remote.creation.compose.state.RemoteInt
import androidx.compose.remote.creation.compose.state.RemoteString
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rf
import androidx.compose.remote.creation.compose.state.ri
import androidx.compose.remote.creation.compose.state.rsp
import androidx.compose.remote.creation.compose.state.sin
import androidx.compose.remote.creation.compose.text.RemoteFontFamily
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
fun brandColors(brand: ConferenceBrand): BrandColors {
    val theme = LocalTemplateScope.current.contract.theme
    return BrandColors(
        primary = inputColor(theme.primary, brand.primary),
        onPrimary = inputColor(theme.onPrimary, brand.onPrimary),
        secondary = inputColor(theme.secondary, brand.secondary),
        tertiary = inputColor(theme.tertiary, brand.tertiary),
        surface = inputColor(theme.surface, brand.surface),
        onSurface = inputColor(theme.onSurface, brand.onSurface),
        muted = inputColor(theme.onSurfaceVariant, brand.muted),
    )
}

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
        fontWeight = weight,
        fontFamily = family?.let { RemoteFontFamily.Named(it.removePrefix("system:")) } ?: RemoteFontFamily.Default,
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
    )
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
