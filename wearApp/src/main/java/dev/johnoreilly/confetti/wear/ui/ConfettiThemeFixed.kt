package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

@Composable
fun ConfettiThemeFixed(
    colors: Colors = Colors(),
    content: @Composable () -> Unit
) {
    MaterialTheme(colors = colors, typography = MaterialTheme.typography.withPlatformStyle()) {
        content()
    }
}