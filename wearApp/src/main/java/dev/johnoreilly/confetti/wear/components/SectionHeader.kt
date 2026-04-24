package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text

/**
 * Inline section divider inside a `TransformingLazyColumn`.
 *
 * Styled with `labelMedium` in the theme's `primary` colour so the seedColor
 * assigned by the active conference is visible at a glance — the bright
 * accent is the connective tissue between the conference and the list rows
 * below it.
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null,
) {
    ListSubHeader(
        modifier = modifier.fillMaxWidth(),
        transformation = transformation,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Top-of-screen title. Uses `titleMedium` on `onSurface` so it reads as the
 * highest-authority element on the screen without fighting the seedColor
 * accent that [SectionHeader] carries below it.
 */
@Composable
fun ScreenHeader(
    text: String,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null,
) {
    ListHeader(
        modifier = modifier.fillMaxWidth(),
        transformation = transformation,
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
