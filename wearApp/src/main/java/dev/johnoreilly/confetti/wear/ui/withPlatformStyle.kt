@file:Suppress("DEPRECATION")

package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.PlatformTextStyle
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Typography

@Suppress("DEPRECATION")
@Composable
fun Typography.withPlatformStyle() = remember(this) {
    val platformStyle = PlatformTextStyle(includeFontPadding = false)
    this.copy(
        display1 = this.display1.copy(platformStyle = platformStyle),
        display2 = this.display2.copy(platformStyle = platformStyle),
        display3 = this.display3.copy(platformStyle = platformStyle),
        title1 = this.title1.copy(platformStyle = platformStyle),
        title2 = this.title2.copy(platformStyle = platformStyle),
        title3 = this.title3.copy(platformStyle = platformStyle),
        body1 = this.body1.copy(platformStyle = platformStyle),
        body2 = this.body2.copy(platformStyle = platformStyle),
        button = this.button.copy(platformStyle = platformStyle),
        caption1 = this.caption1.copy(platformStyle = platformStyle),
        caption2 = this.caption2.copy(platformStyle = platformStyle),
        caption3 = this.caption3.copy(platformStyle = platformStyle),
    )
}

fun Colors.toTileColors(): androidx.wear.protolayout.material.Colors =
    androidx.wear.protolayout.material.Colors(
        primary.toArgb(),
        onPrimary.toArgb(),
        surface.toArgb(),
        onSurface.toArgb()
    )