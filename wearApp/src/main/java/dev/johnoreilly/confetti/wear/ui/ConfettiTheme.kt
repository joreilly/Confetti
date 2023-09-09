package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.wear.compose.material.Colors
import dev.johnoreilly.confetti.wear.proto.Theme
import dev.johnoreilly.confetti.wear.settings.toMaterialThemeColors

/**
 * Confetti theme.
 */
@Composable
fun ConfettiTheme(
    mobileTheme: Theme? = null,
    content: @Composable () -> Unit
) {
    val colors = remember(mobileTheme) {
        mobileTheme?.toMaterialThemeColors() ?: Colors()
    }
    ConfettiThemeFixed(colors = colors, content = content)
}

