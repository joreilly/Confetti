@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.speakerdetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Text
import coil.compose.AsyncImage
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.wear.components.SectionHeader
import org.koin.androidx.compose.getViewModel


@Composable
fun SpeakerDetailsRoute(
    columnState: ScalingLazyColumnState,
    viewModel: SpeakerDetailsViewModel = getViewModel()
) {
    val speaker by viewModel.speaker.collectAsStateWithLifecycle()
    SpeakerDetailsView(speaker, columnState)
}


@Composable
fun SpeakerDetailsView(speaker: SpeakerDetails?, columnState: ScalingLazyColumnState) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState
    ) {
        if (speaker != null) {
            item {
                SectionHeader(text = speaker.name,)
            }

            item {
                val imageUrl = speaker.photoUrl ?: ""
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = speaker.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            val tagline = speaker.tagline
            if (tagline != null) {
                item {
                    Text(
                        text = tagline
                    )
                }
            }

            item {
                Text(
                    modifier = Modifier.padding(bottom = 48.dp),
                    text = speaker.bio ?: ""
                )
            }
        }
    }
}


