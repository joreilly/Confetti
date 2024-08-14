package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import coil.compose.SubcomposeAsyncImage
import com.google.android.horologist.compose.material.Chip
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.fullNameAndCompany
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.icons.Person

val SpeakerDetails.wearPhotoUrl: String?
    get() = photoUrlThumbnail?.let {
        "$it?size=Watch"
    } ?: photoUrl

@Composable
fun SessionSpeakerChip(
    modifier: Modifier = Modifier,
    speaker: SpeakerDetails,
    navigateToSpeaker: (String) -> Unit
) {
    Chip(
        modifier = modifier,
        label = speaker.fullNameAndCompany(),
        icon = {
            SubcomposeAsyncImage(
                model = speaker.wearPhotoUrl,
                contentDescription = speaker.name,
                loading = {
                    CircularProgressIndicator()
                },
                error = {
                    Icon(
                        imageVector = ConfettiIcons.Person,
                        contentDescription = speaker.name,
                        modifier = Modifier
                            .size(ChipDefaults.LargeIconSize)
                            .clip(CircleShape),
                    )
                },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(ChipDefaults.LargeIconSize)
                    .clip(CircleShape)
            )
        },
        secondaryLabel = speaker.tagline,
        onClick = { navigateToSpeaker(speaker.id) }
    )
}

