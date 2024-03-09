package dev.johnoreilly.confetti.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun ThemeSwatches(seedColor: Theme) {
    ConfettiTheme(seedColor = seedColor.color) {
        Column {
            Text(
                text = "onPrimary / primary",
                color = MaterialTheme.colors.onPrimary,
                modifier = Modifier.background(MaterialTheme.colors.primary)
            )
            Text(
                text = "onPrimary / primaryVariant",
                color = MaterialTheme.colors.onPrimary,
                modifier = Modifier.background(MaterialTheme.colors.primaryVariant)
            )
            Text(
                text = "onSecondary / secondary",
                color = MaterialTheme.colors.onSecondary,
                modifier = Modifier.background(MaterialTheme.colors.secondary)
            )
            Text(
                text = "onSecondary / secondaryVariant",
                color = MaterialTheme.colors.onSecondary,
                modifier = Modifier.background(MaterialTheme.colors.secondaryVariant)
            )
            Text(
                text = "onSurface / surface",
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.background(MaterialTheme.colors.surface)
            )
            Text(
                text = "onSurfaceVariant / surface",
                color = MaterialTheme.colors.onSurfaceVariant,
                modifier = Modifier.background(MaterialTheme.colors.surface)
            )
            Text(
                text = "onSurfaceVariant / surface",
                color = MaterialTheme.colors.onSurfaceVariant,
                modifier = Modifier.background(MaterialTheme.colors.surface)
            )
            Text(
                text = "onBackground / background",
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier.background(MaterialTheme.colors.background)
            )
            Text(
                text = "onError / error",
                color = MaterialTheme.colors.onError,
                modifier = Modifier.background(MaterialTheme.colors.error)
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