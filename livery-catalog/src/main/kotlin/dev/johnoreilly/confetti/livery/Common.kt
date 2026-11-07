package dev.johnoreilly.confetti.livery

import androidx.compose.remote.creation.compose.layout.RemoteBox
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

/** 0.45…1.0, once every ~1.5 s, on the player's clock: no recomposition, no re-capture. */
fun pulse(): RemoteFloat = sin(RemoteTimeVariables.continuousSeconds * 4f) * 0.275f + 0.725f

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
fun Chip(text: RemoteString, background: RemoteColor, content: RemoteColor, corner: Int = 50) {
    RemoteBox(
        modifier = RemoteModifier
            .clip(RemoteRoundedCornerShape(corner.rdp))
            .background(background)
            .padding(horizontal = 10.rdp, vertical = 4.rdp),
    ) {
        Label(text, content, size = 12, weight = FontWeight.SemiBold)
    }
}
