@file:OptIn(ExperimentalWearMaterialApi::class)

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
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.rememberPlaceholderState
import coil.compose.SubcomposeAsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.listTextPadding
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import dev.johnoreilly.confetti.decompose.SpeakerDetailsComponent
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.icons.Person
import dev.johnoreilly.confetti.wear.components.SectionHeader

@Composable
fun SpeakerDetailsRoute(
    component: SpeakerDetailsComponent,
) {
    val uiState by component.uiState.subscribeAsState()
    SpeakerDetailsView(uiState)
}

@Composable
fun SpeakerDetailsView(uiState: SpeakerDetailsUiState) {
    val placeholderState = rememberPlaceholderState { uiState !is SpeakerDetailsUiState.Loading }

    val columnState: ScalingLazyColumnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ItemType.Icon,
            last = ItemType.Text
        )
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            columnState = columnState
        ) {
            if (uiState is SpeakerDetailsUiState.Loading) {
                item {
                    Icon(
                        imageVector = ConfettiIcons.Person,
                        contentDescription = "",
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
                    Text(text = "")
                }
            } else {
                val speaker = (uiState as? SpeakerDetailsUiState.Success)?.details

                item {
                    SubcomposeAsyncImage(
                        model = speaker?.photoUrl,
                        contentDescription = speaker?.name,
                        loading = {
                            CircularProgressIndicator()
                        },
                        error = {
                            Icon(
                                imageVector = ConfettiIcons.Person,
                                contentDescription = speaker?.name,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                            )
                        },
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
                            modifier = Modifier.listTextPadding()
                        )
                    }
                }

                if (speaker == null || speaker.bio != null) {
                    item {
                        Text(
                            text = speaker?.bio ?: "",
                            modifier = Modifier.listTextPadding()
                        )
                    }
                }
            }
        }
    }
}


