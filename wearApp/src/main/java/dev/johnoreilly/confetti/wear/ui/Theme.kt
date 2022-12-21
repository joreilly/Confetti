package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.toArgb
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
    content: @Composable() () -> Unit
) {
    MaterialTheme(colors = ColorScheme, content = content)
}

fun Colors.toTileColors(): androidx.wear.tiles.material.Colors =
    androidx.wear.tiles.material.Colors(
        primary.toArgb(),
        onPrimary.toArgb(),
        surface.toArgb(),
        onSurface.toArgb()
    )
