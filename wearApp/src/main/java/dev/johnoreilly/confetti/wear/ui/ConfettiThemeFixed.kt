package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography

@Composable
fun ConfettiThemeFixed(
    colors: ColorScheme = ColorScheme(),
    typography: Typography = Typography(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = colors, typography = typography) {
        content()
    }
}
