package dev.johnoreilly.confetti.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.compose.tools.ThemeValues
import com.google.android.horologist.compose.tools.WearPreview
import dev.johnoreilly.confetti.wear.preview.ConfettiPreviewThemes
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme

@WearPreview
@Composable
fun ThemeColourPreview(
    @PreviewParameter(ConfettiPreviewThemes::class) themeValues: ThemeValues
) {
    ConfettiTheme(colors = themeValues.colors) {
        ThemeSwatches()
    }
}

@Composable
fun ThemeSwatches() {
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