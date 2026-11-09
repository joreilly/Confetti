package dev.johnoreilly.confetti.preview

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import coil3.Image
import coil3.asImage

internal actual fun ImageBitmap.asCoilImage(): Image = asSkiaBitmap().asImage()
