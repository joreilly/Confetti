@file:OptIn(ExperimentalHorologistApi::class, ExperimentalWearMaterialApi::class)

package dev.johnoreilly.confetti.wear.speakerdetails

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.rememberPlaceholderState
import coil.compose.AsyncImage
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.shared.R
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.utils.QueryResult
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
fun SpeakerDetailsView(uiState: QueryResult<SpeakerDetails>, columnState: ScalingLazyColumnState) {
    val placeholderState = rememberPlaceholderState { uiState !is QueryResult.Loading }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState
    ) {

        if (uiState is QueryResult.Loading) {
            item {
                AsyncImage(
                    model = R.drawable.ic_person_black_24dp,
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            item {
                SectionHeader(
                    text = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .height(24.dp)
                        .placeholder(placeholderState)
                )
            }

            item {
                Text(
                    text = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .height(24.dp)
                        .placeholder(placeholderState)
                )
            }

            item {
                Text(
                    text = "",
                )
            }
        } else {
            val speaker = (uiState as? QueryResult.Success)?.result

            item {
                AsyncImage(
                    model = speaker?.photoUrl,
                    contentDescription = speaker?.name ?: "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            item {
                SectionHeader(
                    text = speaker?.name ?: "",
                )
            }

            if (speaker == null || speaker.tagline != null) {
                item {
                    Text(
                        text = speaker?.tagline ?: "",
                    )
                }
            }

            if (speaker == null || speaker.bio != null) {
                item {
                    Text(
                        text = speaker?.bio ?: "",
                        modifier = Modifier.padding(bottom = 48.dp),
                    )
                }
            }
        }
    }
}


