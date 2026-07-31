package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography
import dev.johnoreilly.confetti.ui.ConsumePreviewThemeOverride
import dev.johnoreilly.confetti.ui.LocalPreviewThemeOverride

/**
 * The single funnel every Confetti Wear theme goes through — [ConfettiTheme] and the preview
 * scaffold both end up here, so it is also the one place that has to honour a preview theme
 * override.
 *
 * When [LocalPreviewThemeOverride] is set, a `@WearThemeCatalog` provider has already installed the
 * theme the viewer asked for; installing ours on top would shadow it and pin every theme in the
 * switcher to identical pixels. Stand down instead, clearing the flag so a deliberately nested
 * theme deeper in the tree still applies. See `PreviewThemeOverride` in `:shared`.
 */
@Composable
fun ConfettiThemeFixed(
    colors: ColorScheme = ColorScheme(),
    typography: Typography = Typography(),
    content: @Composable () -> Unit,
) {
    if (LocalPreviewThemeOverride.current) {
        ConsumePreviewThemeOverride(content)
        return
    }
    MaterialTheme(colorScheme = colors, typography = typography) {
        content()
    }
}
