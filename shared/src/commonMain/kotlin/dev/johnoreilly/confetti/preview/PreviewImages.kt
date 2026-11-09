package dev.johnoreilly.confetti.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import coil3.ColorImage
import coil3.Image
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Previews never fetch images. A preview that shows a speaker photo wraps its content in this, so
 * every `AsyncImage` resolves through Coil's preview handler instead of the network: the two
 * preview speakers ([JohnUrl], [MartinUrl]) draw their photo, anything else a fixed placeholder.
 *
 * A network fetch in a preview finishes in real time, after the renderer has captured the frame
 * (a settled capture only advances the preview's virtual clock), so it shows a loading spinner
 * or not depending on the runner's network. The photos are compiled into common code
 * ([PreviewPhotos.kt]) and decoded synchronously here, so the capture is the same on every
 * platform and renderer, with no resources, LFS checkout or network involved.
 */
@Composable
fun PreviewImages(content: @Composable () -> Unit) {
    val handler = remember {
        AsyncImagePreviewHandler { request -> previewImage(request.data?.toString()) }
    }
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides handler, content = content)
}

/** What a preview draws for an image [url]: a preview speaker's photo, or the placeholder. */
internal fun previewImage(url: String?): Image =
    previewPhoto(previewPhotoName(url)) ?: ColorImage(color = 0xFF6E56CF.toInt(), width = 256, height = 256)

/** The bundled photo's name for a preview speaker's URL, or null. */
internal fun previewPhotoName(url: String?): String? = when {
    url == null -> null
    url.contains("HkquSQhsfczBGkrABwVTBc") -> "preview_speaker_john"
    url.contains("UiWeCMZDxPejrFsozKmLYr") -> "preview_speaker_martin"
    else -> null
}

/** A preview speaker's photo by [name], decoded once. */
internal fun previewPhoto(name: String?): Image? = when (name) {
    "preview_speaker_john" -> PreviewPhotos.john
    "preview_speaker_martin" -> PreviewPhotos.martin
    else -> null
}

@OptIn(ExperimentalEncodingApi::class)
private object PreviewPhotos {
    val john: Image by lazy { decode(JohnPhotoJpegBase64) }
    val martin: Image by lazy { decode(MartinPhotoJpegBase64) }

    private fun decode(base64: String): Image = Base64.decode(base64).decodeToImageBitmap().asCoilImage()
}

/** This platform's Coil [Image] for a decoded bitmap. */
internal expect fun ImageBitmap.asCoilImage(): Image
