package dev.johnoreilly.confetti.ui.speakers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import dev.johnoreilly.confetti.avatarUrl
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.preview.sampleSpeakers
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.icons.Person


@Composable
fun SpeakerGridView(
    conference: String,
    speakers: List<SpeakerDetails>,
    navigateToSpeaker: (id: String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(12.dp),
        content = {
            items(speakers) { speaker ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable { navigateToSpeaker(speaker.id) },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SubcomposeAsyncImage(
                            model = speaker.avatarUrl(),
                            contentDescription = speaker.name,
                            loading = {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            },
                            error = {
                                Icon(
                                    imageVector = ConfettiIcons.Person,
                                    contentDescription = speaker.name,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = speaker.name,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        speaker.tagline?.let { company ->
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = company,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    )
}

@MobilePreviews
@Composable
internal fun SpeakerGridViewLoadedPreview() {
    SpeakerGridView(
        conference = "kotlinconf2023",
        speakers = sampleSpeakers,
        navigateToSpeaker = {},
    )
}

@Preview(name = "Empty", widthDp = 411, heightDp = 914, showBackground = true)
@Composable
internal fun SpeakerGridViewEmptyPreview() {
    SpeakerGridView(
        conference = "kotlinconf2023",
        speakers = emptyList(),
        navigateToSpeaker = {},
    )
}