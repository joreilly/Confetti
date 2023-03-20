package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.PlatformTextStyle
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import dev.johnoreilly.confetti.wear.proto.Theme
import dev.johnoreilly.confetti.wear.settings.toMaterialThemeColors


/**
 * Dark Android theme color scheme
 */
val ColorScheme = Colors(
    primary = Green80,
    onPrimary = Green20,
    secondary = DarkGreen80,
    onSecondary = DarkGreen20,
    error = Red80,
    onError = Red20,
    background = Black,
    onBackground = DarkGreenGray90,
    surface = DarkGreenGray10,
    onSurface = DarkGreenGray90,
    onSurfaceVariant = GreenGray80,
)

/**
 * Confetti theme.
 */
@Suppress("DEPRECATION")
@Composable
fun ConfettiTheme(
    mobileTheme: Theme? = null,
    content: @Composable () -> Unit
) {
    val material = MaterialTheme.typography
    val typography = remember(material) {
        val platformStyle = PlatformTextStyle(includeFontPadding = false)
        material.copy(
            display1 = material.display1.copy(platformStyle = platformStyle),
            display2 = material.display2.copy(platformStyle = platformStyle),
            display3 = material.display3.copy(platformStyle = platformStyle),
            title1 = material.title1.copy(platformStyle = platformStyle),
            title2 = material.title2.copy(platformStyle = platformStyle),
            title3 = material.title3.copy(platformStyle = platformStyle),
            body1 = material.body1.copy(platformStyle = platformStyle),
            body2 = material.body2.copy(platformStyle = platformStyle),
            button = material.button.copy(platformStyle = platformStyle),
            caption1 = material.caption1.copy(platformStyle = platformStyle),
            caption2 = material.caption2.copy(platformStyle = platformStyle),
            caption3 = material.caption3.copy(platformStyle = platformStyle),
        )
    }
    val colors = remember(mobileTheme) {
        mobileTheme?.toMaterialThemeColors() ?: ColorScheme
    }
    MaterialTheme(colors = colors, content = content, typography = typography)
}

fun Colors.toTileColors(): androidx.wear.tiles.material.Colors =
    androidx.wear.tiles.material.Colors(
        primary.toArgb(),
        onPrimary.toArgb(),
        surface.toArgb(),
        onSurface.toArgb()
    )
