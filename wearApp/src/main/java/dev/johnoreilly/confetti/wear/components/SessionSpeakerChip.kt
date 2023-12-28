package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import coil.compose.SubcomposeAsyncImage
import com.google.android.horologist.compose.material.Chip
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.fullNameAndCompany
import dev.johnoreilly.confetti.shared.R

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
                model = speaker.photoUrlThumbnail?.let {
                    "$it?size=Watch"
                },
                contentDescription = speaker.name,
                loading = {
                    CircularProgressIndicator()
                },
                error = {
                    Image(
                        painter = painterResource(R.drawable.ic_person_black_24dp),
                        contentDescription = speaker.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(ChipDefaults.LargeIconSize)
                            .clip(CircleShape),
                        colorFilter = ColorFilter.tint(color = if (isSystemInDarkTheme()) Color.White else Color.Black)
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

