package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.CircleShape
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.icons.Person

/**
 * A deterministic stand-in for a speaker photo: a tinted [shape] with a person glyph.
 *
 * Shown when a real image can't load — chiefly the design-catalog / `@Preview` render, where
 * `LocalInspectionMode` is true and Coil never executes the network request, so a
 * `SubcomposeAsyncImage` would otherwise sit on its loading spinner (an empty ring) forever. Reads
 * as an avatar instead. Production is unaffected: real loads still resolve to the photo, and this is
 * only substituted for the spinner while inspecting.
 */
@Composable
fun AvatarPlaceholder(
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
) {
    Box(
        modifier = modifier.clip(shape).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = ConfettiIcons.Person,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.fillMaxSize(0.6f),
        )
    }
}
