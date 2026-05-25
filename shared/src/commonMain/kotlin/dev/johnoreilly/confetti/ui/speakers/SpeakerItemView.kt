package dev.johnoreilly.confetti.ui.speakers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.preview.johnOreillySpeaker
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.icons.Person

@Composable
fun SpeakerItemView(
    speaker: SpeakerDetails,
    navigateToSpeaker: (id: String) -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { navigateToSpeaker(speaker.id) }),
        headlineContent = {
            Text(text = speaker.name)
        },
        supportingContent = speaker.tagline?.let { company ->
            {
                Text(company)
            }
        },
        leadingContent = {
            SubcomposeAsyncImage(
                model = speaker.photoUrl,
                contentDescription = speaker.name,
                loading = {
                    CircularProgressIndicator()
                },
                error = {
                    Icon(
                        imageVector = ConfettiIcons.Person,
                        contentDescription = speaker.name,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                    )
                },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
        }
    )
}

@MobilePreviews
@Composable
internal fun SpeakerItemViewLoadedPreview() {
    SpeakerItemView(
        speaker = johnOreillySpeaker,
        navigateToSpeaker = {},
    )
}

@Preview(name = "No tagline", widthDp = 411, heightDp = 100, showBackground = true)
@Composable
internal fun SpeakerItemViewNoTaglinePreview() {
    SpeakerItemView(
        speaker = johnOreillySpeaker.copy(tagline = null),
        navigateToSpeaker = {},
    )
}
