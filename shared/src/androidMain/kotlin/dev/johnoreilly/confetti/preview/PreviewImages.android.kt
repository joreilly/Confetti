package dev.johnoreilly.confetti.preview

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil3.Image
import coil3.asImage

/**
 * The photos are the app's debug `res/raw/preview_speaker_*.jpg` (LFS, shared with the screenshot
 * fixtures), so they never ship in a release build. A module without them (`shared`'s own renders)
 * gets null, and the caller's placeholder.
 */
@Composable
internal actual fun rememberBundledPreviewImages(): (name: String?) -> Image? {
    val context = LocalContext.current
    return remember(context) {
        { name ->
            name?.let { context.resources.getIdentifier(it, "raw", context.packageName) }?.takeIf { it != 0 }?.let { id ->
                runCatching { context.resources.openRawResource(id).use { BitmapFactory.decodeStream(it) }?.asImage() }.getOrNull()
            }
        }
    }
}
