package dev.johnoreilly.confetti.preview

import androidx.compose.runtime.Composable
import coil3.Image

/** No bundled preview photos here: previews draw the placeholder. */
@Composable
internal actual fun rememberBundledPreviewImages(): (name: String?) -> Image? = { null }
