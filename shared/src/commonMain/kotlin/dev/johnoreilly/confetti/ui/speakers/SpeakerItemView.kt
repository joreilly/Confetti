package dev.johnoreilly.confetti.ui.speakers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import dev.johnoreilly.confetti.ui.component.FullScreenPhotoDialog
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import dev.johnoreilly.confetti.avatarUrl
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
    var showFullScreenPhoto by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { navigateToSpeaker(speaker.id) },
        headlineContent = {
            Text(
                text = speaker.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = if (speaker.tagline != null) {
            {
                Text(
                    text = speaker.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            null
        },
        leadingContent = {
            SubcomposeAsyncImage(
                model = speaker.avatarUrl(),
                contentDescription = speaker.name,
                loading = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                },
                error = {
                    Icon(
                        imageVector = ConfettiIcons.Person,
                        contentDescription = speaker.name,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { showFullScreenPhoto = true }
            )
        }
    )

    if (showFullScreenPhoto) {
        FullScreenPhotoDialog(
            photoUrl = speaker.avatarUrl(),
            contentDescription = speaker.name,
            onDismissRequest = { showFullScreenPhoto = false }
        )
    }
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
