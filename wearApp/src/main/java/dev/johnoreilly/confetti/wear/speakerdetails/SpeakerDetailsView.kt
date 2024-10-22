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
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.rememberPlaceholderState
import coil.compose.SubcomposeAsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import androidx.wear.compose.material3.ScreenScaffold
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

    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
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
                        )
                    }
                }

                if (speaker == null || speaker.bio != null) {
                    item {
                        Text(
                            text = speaker?.bio ?: "",
                        )
                    }
                }
            }
        }
    }
}


