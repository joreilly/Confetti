package dev.johnoreilly.confetti.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect

/**
 * Haze 2 equivalent of Haze 1's `HazeStyle(backgroundColor, tint = HazeTint(color), blurRadius)`.
 * [HazeBlurStyle]s compare by identity, so remember one per set of inputs.
 */
@Composable
internal fun rememberHazeBlurStyle(background: Color, tint: Color, radius: Dp): HazeBlurStyle =
    remember(background, tint, radius) {
        HazeBlurStyle {
            backgroundColor(background)
            colorEffects(listOf(HazeColorEffect.tint(tint)))
            blurRadius(radius)
        }
    }
