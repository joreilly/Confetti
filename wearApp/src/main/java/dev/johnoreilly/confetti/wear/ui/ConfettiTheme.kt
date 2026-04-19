@file:OptIn(ExperimentalStdlibApi::class)

package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.Typography
import com.materialkolor.rememberDynamicColorScheme

/**
 * Confetti theme.
 *
 * Given a conference [seedColor] we build a Material 3 dynamic dark scheme
 * via `materialkolor` and map every role it defines onto the Wear [ColorScheme]
 * — including the `surfaceContainer*` family, outlines, and error/tertiary.
 * Without a seed we fall back to the Wear defaults.
 *
 * [typography] defaults to [ExpressiveTypography] — Roboto Flex + Inter,
 * the ship configuration described in `design/STYLE_GUIDE.md`.
 */
@Composable
fun ConfettiTheme(
    seedColor: Color?,
    typography: Typography = ExpressiveTypography,
    content: @Composable () -> Unit,
) {
    val colors = if (seedColor != null) {
        val colorScheme = rememberDynamicColorScheme(seedColor = seedColor, isDark = true, isAmoled = false)
        colorScheme.toWearMaterialColors()
    } else {
        ColorScheme()
    }

    ConfettiThemeFixed(colors = colors, typography = typography, content = content)
}

/**
 * Map a Material 3 (mobile) [androidx.compose.material3.ColorScheme] onto the
 * Wear Material 3 [ColorScheme]. Wear has its own token names for some roles:
 * - `surfaceContainerLowest` (M3) → `background` (Wear) — darkest surface,
 *   matches what the round-face bezel blends into.
 * - `primaryContainer` / `onPrimaryContainer` carry through directly; ditto
 *   secondary and tertiary containers.
 * - Wear's `*Dim` roles (primaryDim, secondaryDim, tertiaryDim, errorDim)
 *   aren't in M3, so we reuse the container colour — close enough visually
 *   for dimmed states on round displays.
 */
fun androidx.compose.material3.ColorScheme.toWearMaterialColors(): ColorScheme = ColorScheme(
    primary = primary,
    primaryDim = primaryContainer,
    primaryContainer = primaryContainer,
    onPrimary = onPrimary,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    secondaryDim = secondaryContainer,
    secondaryContainer = secondaryContainer,
    onSecondary = onSecondary,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    tertiaryDim = tertiaryContainer,
    tertiaryContainer = tertiaryContainer,
    onTertiary = onTertiary,
    onTertiaryContainer = onTertiaryContainer,
    surfaceContainerLow = surfaceContainerLow,
    surfaceContainer = surfaceContainer,
    surfaceContainerHigh = surfaceContainerHigh,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    outlineVariant = outlineVariant,
    background = surfaceContainerLowest,
    onBackground = onBackground,
    error = error,
    errorDim = errorContainer,
    errorContainer = errorContainer,
    onError = onError,
    onErrorContainer = onErrorContainer,
)

fun String?.toColor(): Color {
    return this?.let {
        runCatching {
            Color(hexToLong(HexFormat { number.prefix = "0x" }))
        }
    }?.getOrNull() ?: Color(0xFF008000) // default if none set
}
