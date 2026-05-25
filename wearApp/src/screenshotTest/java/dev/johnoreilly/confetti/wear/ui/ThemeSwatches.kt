package dev.johnoreilly.confetti.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun ThemeSwatches(seedColor: Theme) {
    ConfettiTheme(seedColor = seedColor.color) {
        Column {
            Text(
                text = "onPrimary / primary",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.background(MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "onPrimary / primaryVariant",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryDim)
            )
            Text(
                text = "onSecondary / secondary",
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondary)
            )
            Text(
                text = "onSecondary / secondaryVariant",
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryDim)
            )
            Text(
                text = "onSurface / surface",
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
            )
            Text(
                text = "onSurfaceVariant / surface",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
            )
            Text(
                text = "onSurfaceVariant / surface",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
            Text(
                text = "onBackground / background",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            )
            Text(
                text = "onError / error",
                color = MaterialTheme.colorScheme.onError,
                modifier = Modifier.background(MaterialTheme.colorScheme.error)
            )
        }
    }
}

@Preview
@Composable
fun DefaultsSwatches() {
    ThemeSwatches(seedColor = Theme("", null))
}

@Preview
@Composable
fun NotSetSwatches() {
    ThemeSwatches(seedColor = Theme("", Color.Black))
}

@Preview
@Composable
fun Color_0x800000Swatches() {
    ThemeSwatches(seedColor = Theme("0x800000"))
}

@Preview
@Composable
fun Color_0x008000Swatches() {
    ThemeSwatches(seedColor = Theme("0x008000"))
}

@Preview
@Composable
fun Color_0x000080Swatches() {
    ThemeSwatches(seedColor = Theme("0x000080"))
}