package dev.johnoreilly.confetti.preview

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil3.BitmapImage
import coil3.ColorImage
import coil3.compose.AsyncImage
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image as SkiaImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The preview speakers' photos are compiled in and decoded synchronously, so a preview's first
 * frame already shows them: no resources, LFS checkout or network can make a render differ.
 */
class PreviewImagesTest {
    @Test
    fun bothPreviewSpeakersHaveTheirPhoto() {
        for (url in listOf(JohnUrl, MartinUrl)) {
            val image = assertIs<BitmapImage>(previewImage(url), url)
            assertEquals(192, image.width)
            assertEquals(192, image.height)
        }
    }

    @Test
    fun anyOtherImageIsThePlaceholder() {
        assertIs<ColorImage>(previewImage("https://example.com/someone.jpg"))
        assertIs<ColorImage>(previewImage(null))
    }

    @Test
    fun aPhotoIsDecodedOnce() {
        assertSame(previewImage(JohnUrl), previewImage(JohnUrl))
    }

    @Test
    fun thePreviewsFirstFrameDrawsThePhoto() {
        val scene = ImageComposeScene(width = 100, height = 100, density = Density(1f)) {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewImages {
                    AsyncImage(
                        model = JohnUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp),
                    )
                }
            }
        }
        val frame = try {
            scene.render(0L)
        } finally {
            scene.close()
        }
        val pixels = Bitmap.makeFromImage(frame)
        val colors = (0 until 100 step 10).flatMap { x -> (0 until 100 step 10).map { y -> pixels.getColor(x, y) } }.toSet()
        // A photo, not the flat placeholder (0xFF6E56CF) or an empty frame.
        assertTrue(colors.size > 20, "expected a photo, got ${colors.size} distinct colours")
    }
}
