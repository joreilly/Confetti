package dev.johnoreilly.confetti.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import coil3.ColorImage
import coil3.Image
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler

/**
 * Previews never fetch images. A preview that shows a speaker photo wraps its content in this, so
 * every `AsyncImage` resolves through Coil's preview handler instead of the network: the two
 * preview speakers ([JohnUrl], [MartinUrl]) draw their bundled photo where the platform has one
 * (Android, from the app's debug `res/raw`), anything else a fixed placeholder.
 *
 * A network fetch in a preview finishes in real time, after the renderer has captured the frame
 * (a settled capture only advances the preview's virtual clock), so it shows a loading spinner
 * or not depending on the runner's network. Bundled images make the capture the same every time.
 */
@Composable
fun PreviewImages(content: @Composable () -> Unit) {
    val bundled = rememberBundledPreviewImages()
    val handler = remember(bundled) {
        AsyncImagePreviewHandler { request ->
            bundled(previewPhotoName(request.data?.toString())) ?: ColorImage(color = 0xFF6E56CF.toInt(), width = 256, height = 256)
        }
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides handler, content = content)
}

/** The bundled photo's resource name for a preview speaker's URL, or null. */
internal fun previewPhotoName(url: String?): String? = when {
    url == null -> null
    url.contains("HkquSQhsfczBGkrABwVTBc") -> "preview_speaker_john"
    url.contains("UiWeCMZDxPejrFsozKmLYr") -> "preview_speaker_martin"
    else -> null
}

/** Loads a bundled preview photo by resource name, or null where the platform has none. */
@Composable
internal expect fun rememberBundledPreviewImages(): (name: String?) -> Image?
