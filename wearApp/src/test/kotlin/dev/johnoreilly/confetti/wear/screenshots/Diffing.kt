package dev.johnoreilly.confetti.wear.screenshots

import android.graphics.Bitmap
import android.util.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.SemanticsNodeInteraction
import com.quickbird.snapshot.Diffing
import com.quickbird.snapshot.FileSnapshotting
import com.quickbird.snapshot.FileStoring
import com.quickbird.snapshot.Snapshotting
import com.quickbird.snapshot.bitmap
import com.quickbird.snapshot.fileSnapshotting
import okio.Buffer
import okio.ByteString
import kotlin.math.ceil
import kotlin.math.roundToInt

val Diffing.Companion.highlightWithRed
    get() = Diffing<Color> { first, second ->
        if (first == second)
            first.copy(alpha = first.alpha / 3f)
        else
            Color.Red
    }

val Bitmap.size: Size
    get() = Size(width, height)

fun Bitmap.eachPixel(fn: (Int, Int) -> Unit) {
    val size = this.size
    for (x in 0 until size.width) {
        for (y in 0 until size.height) {
            fn(x, y)
        }
    }
}

fun Diffing.Companion.bitmapWithTolerance(tolerance: Float, colorDiffing: Diffing<Color>) =
    Diffing<Bitmap> { originalBitmap, newBitmap ->
        val originalBytes = originalBitmap.asByteString()
        val newBytes = newBitmap.asByteString()
        if (originalBitmap.size != newBitmap.size) {
            newBitmap
        } else if (originalBytes == newBytes) {
            null
        } else {
            var differentCount = 0
            val diffBitmap = originalBitmap.copy(originalBitmap.config, true).apply {
                eachPixel { x, y ->
                    val originalColor = Color(originalBitmap.getPixel(x, y))
                    val newColor = Color(newBitmap.getPixel(x, y))

                    if (originalColor != newColor) {
                        differentCount += 1
                    }

                    val diffColor = colorDiffing(originalColor, newColor) ?: originalColor
                    setPixel(x, y, diffColor.toArgb())
                }
            }
            val pixelCount = originalBitmap.width * originalBitmap.height
            val toleratedDiffs = ceil(tolerance * pixelCount).toInt()
            if (differentCount <= toleratedDiffs) {
                null
            } else {
                val diffPercent = differentCount.toDouble() / pixelCount
                println("$diffPercent")
                diffBitmap
            }
        }
    }

fun Bitmap.asByteString(): ByteString = Buffer().apply {
    compress(Bitmap.CompressFormat.PNG, 0, outputStream())
}.readByteString()


fun Snapshotting<SemanticsNodeInteraction, Bitmap>.fileSnapshottingX()
    = FileSnapshotting(fileStoring = FileStoring.bitmap, snapshotting = this)