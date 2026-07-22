package dev.johnoreilly.confetti.wear.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.icons.Person

/**
 * A speaker image for an **inspection / design-catalog render**, where `LocalInspectionMode` is true
 * and Coil never executes the network request (so a `SubcomposeAsyncImage` would sit on its loading
 * spinner — an empty ring — forever). Shows the bundled fixture photo for a known speaker
 * ([bundledSpeakerPhoto]: John / Martin), else an [AvatarPlaceholder]. Call it from a `loading` slot;
 * production is unaffected (real loads still resolve to the network photo).
 */
@Composable
fun InspectionSpeakerImage(
    photoUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
) {
    val photo = bundledSpeakerPhoto(photoUrl)
    if (photo != null) {
        Image(
            bitmap = photo,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape),
        )
    } else {
        AvatarPlaceholder(contentDescription, modifier, shape)
    }
}

/**
 * The bundled preview photo for a known speaker URL, or null. The images are the real speaker photos,
 * reused (via LFS) from the screenshot-test fixtures as `res/raw` in the debug source set — so they
 * ride the design-catalog (debug) render but never ship in release. Decodes defensively: a missing
 * or unreadable resource simply yields null and the caller falls back to a placeholder.
 */
@Composable
private fun bundledSpeakerPhoto(photoUrl: String?): ImageBitmap? {
    val resourceName =
        when {
            photoUrl == null -> return null
            // Match by the sessionize image id so the ?size / thumbnail variants still resolve.
            photoUrl.contains("HkquSQhsfczBGkrABwVTBc") -> "preview_speaker_john"
            photoUrl.contains("UiWeCMZDxPejrFsozKmLYr") -> "preview_speaker_martin"
            else -> return null
        }
    val context = LocalContext.current
    return remember(resourceName) {
        val id = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (id == 0) {
            null
        } else {
            runCatching {
                    context.resources.openRawResource(id).use { BitmapFactory.decodeStream(it) }
                }
                .getOrNull()
                ?.asImageBitmap()
        }
    }
}

/**
 * A deterministic stand-in for a speaker photo: a tinted [shape] with a person glyph — the fallback
 * when no bundled photo matches (see [InspectionSpeakerImage]).
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
