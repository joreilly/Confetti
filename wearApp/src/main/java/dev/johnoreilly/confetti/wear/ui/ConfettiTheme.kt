@file:OptIn(ExperimentalStdlibApi::class)

package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import com.materialkolor.rememberDynamicColorScheme

/**
 * Confetti theme.
 */
@Composable
fun ConfettiTheme(
    seedColor: Color?,
    content: @Composable () -> Unit,
) {
    val colors = if (seedColor != null) {
        val colorScheme = rememberDynamicColorScheme(seedColor = seedColor, isDark = true, isAmoled = false)
        // TODO
        colorScheme.toWearMaterialColors()
    } else {
        ColorScheme()
    }

    ConfettiThemeFixed(colors = colors, content = content)
}

fun androidx.compose.material3.ColorScheme.toWearMaterialColors(): ColorScheme {
    return ColorScheme(
        primary = this.primary,
//        primaryVariant = this.primaryContainer,
        secondary = this.secondary,
//        secondaryVariant = this.secondaryContainer,
        background = Color.Black,
//        surface = this.surface,
        error = this.error,
        onPrimary = this.onPrimary,
        onSecondary = this.onSecondary,
        onBackground = this.onBackground,
        onSurface = this.onSurface,
        onSurfaceVariant = this.onSurfaceVariant,
        onError = this.onError
    )
}

fun String?.toColor(): Color {
    return this?.let {
        runCatching {
            Color(hexToLong(HexFormat { number.prefix = "0x" }))
        }
    }?.getOrNull() ?: Color(0xFF008000) // default if none set
}

