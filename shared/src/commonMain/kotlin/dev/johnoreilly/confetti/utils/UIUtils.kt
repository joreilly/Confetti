package dev.johnoreilly.confetti.utils

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.toSize

val WindowSizeClass.isExpanded: Boolean
    get() = widthSizeClass == WindowWidthSizeClass.Expanded


val WindowSizeClass.isCompact: Boolean
    get() = widthSizeClass == WindowWidthSizeClass.Compact


/**
 * Replacement for material3-window-size-class-multiplatform's own
 * `calculateWindowSizeClass()`. Its desktop actual reaches for
 * `androidx.compose.ui.window.LocalWindow`, which Compose Multiplatform 1.12 removed, so calling it
 * throws NoClassDefFoundError at first composition on desktop. Deriving the size from
 * LocalWindowInfo instead uses only common Compose UI API, so one implementation serves every
 * target.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun currentWindowSizeClass(): WindowSizeClass =
    WindowSizeClass.calculateFromSize(
        size = LocalWindowInfo.current.containerSize.toSize(),
        density = LocalDensity.current,
    )
