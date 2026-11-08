package dev.johnoreilly.confetti.preview

import androidx.compose.runtime.Composable
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import dev.johnoreilly.confetti.ui.components.NativeComponents

/**
 * A preview or screenshot of a composable that draws conference components. Previews have no
 * caller to supply the [ConferenceComponents] context, so this does: [components], Confetti's own
 * by default, with speaker photos resolved without the network ([PreviewImages]).
 */
@Composable
fun ComponentsPreview(
    components: ConferenceComponents = NativeComponents(),
    content: @Composable context(ConferenceComponents) () -> Unit,
) {
    PreviewImages { content(components) }
}
