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
import dev.johnoreilly.confetti.wear.ui.TypographyChoice
import dev.johnoreilly.confetti.wear.ui.typographyFor

/**
 * Preview wrapper that mirrors production scaffolding: [MaterialTheme] +
 * [AppScaffold] with a fixed-time [TimeText] so rendered PNGs include the
 * system time and are deterministic across CI runs.
 *
 * Pass [typography] (or the convenience [typographyChoice]) to fan out a
 * single preview across font families without duplicating the screen body.
 */
@Composable
fun ConfettiPreviewScaffold(
    colors: ColorScheme = ColorScheme(),
    typography: Typography = Typography(),
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

/** Overload that picks a typography by [TypographyChoice]. */
@Composable
fun ConfettiPreviewScaffold(
    typographyChoice: TypographyChoice,
    colors: ColorScheme = ColorScheme(),
    content: @Composable () -> Unit,
) {
    ConfettiPreviewScaffold(
        colors = colors,
        typography = typographyFor(typographyChoice),
        content = content,
    )
}
