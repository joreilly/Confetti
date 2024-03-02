@file:OptIn(ExperimentalWearMaterialApi::class)

package dev.johnoreilly.confetti.wear.speakerdetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.placeholder
import androidx.wear.compose.material.rememberPlaceholderState
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.listTextPadding
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.Title
import dev.johnoreilly.confetti.decompose.SpeakerDetailsComponent
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState
import dev.johnoreilly.confetti.shared.R

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
                    Title(
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
                            .listTextPadding()
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
                            Image(
                                painter = painterResource(R.drawable.ic_person_black_24dp),
                                contentDescription = speaker?.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                colorFilter = ColorFilter.tint(color = if (isSystemInDarkTheme()) Color.White else Color.Black)
                            )
                        },
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                item {
                    Title(
                        text = speaker?.name ?: "",
                        modifier = Modifier.listTextPadding()
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


