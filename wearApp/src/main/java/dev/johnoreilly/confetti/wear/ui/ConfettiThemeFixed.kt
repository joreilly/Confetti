package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun ConfettiThemeFixed(
    colors: ColorScheme = ColorScheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = colors, typography = MaterialTheme.typography) {
        content()
    }
}