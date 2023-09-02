@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.wear.compose.material.ChipDefaults
import coil.compose.AsyncImage
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.material.Chip
import dev.johnoreilly.confetti.shared.R
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.fullNameAndCompany
import dev.johnoreilly.confetti.navigation.SpeakerDetailsKey

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
            AsyncImage(
                modifier = Modifier
                    .size(ChipDefaults.LargeIconSize)
                    .clip(CircleShape),
                placeholder = painterResource(R.drawable.ic_person_black_24dp),
                error = painterResource(R.drawable.ic_person_black_24dp),
                model = speaker.photoUrl,
                contentDescription = speaker.name,
            )
        },
        secondaryLabel = speaker.tagline,
        onClick = { navigateToSpeaker(speaker.id) }
    )
}

