package dev.johnoreilly.confetti.wear.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.Typography
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import dev.johnoreilly.confetti.wear.ui.ExpressiveTypography

/**
 * Preview wrapper that mirrors production scaffolding: [MaterialTheme] +
 * [AppScaffold] with a fixed-time [TimeText] so rendered PNGs include the
 * system time and are deterministic across CI runs. Defaults to the ship
 * [ExpressiveTypography]; override [typography] for design explorations.
 */
@Composable
fun ConfettiPreviewScaffold(
    colors: ColorScheme = ColorScheme(),
    typography: Typography = ExpressiveTypography,
    content: @Composable () -> Unit,
) {
    ConfettiThemeFixed(colors = colors, typography = typography) {
        AppScaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            timeText = { TimeText(timeSource = FixedTimeSource) },
        ) {
            content()
        }
    }
}
