package dev.johnoreilly.confetti.wear.ui

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme


/**
 * Dark Android theme color scheme
 */
@VisibleForTesting
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
