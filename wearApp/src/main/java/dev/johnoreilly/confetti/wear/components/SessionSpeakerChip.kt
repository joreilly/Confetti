package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import coil.compose.SubcomposeAsyncImage
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
    Button(
        modifier = modifier,
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
                            .size(ButtonDefaults.LargeIconSize)
                            .clip(CircleShape),
                    )
                },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(ButtonDefaults.LargeIconSize)
                    .clip(CircleShape)
            )
        },
        secondaryLabel = speaker.tagline?.let {
            {
                Text(it)
            }
        },
        onClick = { navigateToSpeaker(speaker.id) }
    ) {
        Text(speaker.fullNameAndCompany())
    }
}

