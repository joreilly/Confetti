package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.PlatformTextStyle
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme


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
@Composable
fun ConfettiTheme(
    content: @Composable () -> Unit
) {
    val defaultTypography = MaterialTheme.typography
    @Suppress("DEPRECATION")
    val typography = remember {
        defaultTypography.copy(
            display1 = defaultTypography.display1.copy(platformStyle = PlatformTextStyle(false)),
            display2 = defaultTypography.display2.copy(platformStyle = PlatformTextStyle(false)),
            display3 = defaultTypography.display3.copy(platformStyle = PlatformTextStyle(false)),
            title1 = defaultTypography.title1.copy(platformStyle = PlatformTextStyle(false)),
            title2 = defaultTypography.title2.copy(platformStyle = PlatformTextStyle(false)),
            title3 = defaultTypography.title3.copy(platformStyle = PlatformTextStyle(false)),
            body1 = defaultTypography.body1.copy(platformStyle = PlatformTextStyle(false)),
            body2 = defaultTypography.body2.copy(platformStyle = PlatformTextStyle(false)),
            button = defaultTypography.button.copy(platformStyle = PlatformTextStyle(false)),
            caption1 = defaultTypography.caption1.copy(platformStyle = PlatformTextStyle(false)),
            caption2 = defaultTypography.caption2.copy(platformStyle = PlatformTextStyle(false)),
            caption3 = defaultTypography.caption3.copy(platformStyle = PlatformTextStyle(false)),
        )
    }
    MaterialTheme(colors = ColorScheme, content = content, typography = typography)
}

fun Colors.toTileColors(): androidx.wear.tiles.material.Colors =
    androidx.wear.tiles.material.Colors(
        primary.toArgb(),
        onPrimary.toArgb(),
        surface.toArgb(),
        onSurface.toArgb()
    )
