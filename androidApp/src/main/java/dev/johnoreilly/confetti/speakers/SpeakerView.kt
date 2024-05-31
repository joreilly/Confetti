package dev.johnoreilly.confetti.speakers

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.speakers
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.decompose.SpeakersComponent
import dev.johnoreilly.confetti.decompose.SpeakersUiState
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.HomeScaffold
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.SpeakerGridView
import dev.johnoreilly.confetti.ui.isExpanded
import org.jetbrains.compose.resources.stringResource

@Composable
fun SpeakersRoute(
    component: SpeakersComponent,
    windowSizeClass: WindowSizeClass,
    topBarActions: @Composable RowScope.() -> Unit,
) {
    val uiState by component.uiState.subscribeAsState()

    HomeScaffold(
        title = stringResource(Res.string.speakers),
        windowSizeClass = windowSizeClass,
        topBarActions = topBarActions,
    ) {
        when (val state = uiState) {
            is SpeakersUiState.Success -> {
                if (windowSizeClass.isExpanded) {
                    SpeakerGridView(state.speakers, component::onSpeakerClicked)
                } else {
                    SpeakerListView(state.speakers, component::onSpeakerClicked)
                }
            }
            is SpeakersUiState.Loading -> LoadingView()
            is SpeakersUiState.Error -> ErrorView {}
        }
    }
}



@Composable
fun SpeakerListView(
    speakers: List<SpeakerDetails>,
    navigateToSpeaker: (id: String) -> Unit
) {
    Column {
        if (speakers.isNotEmpty()) {
            LazyColumn {
                items(speakers) { speaker ->
                    SpeakerItemView(speaker, navigateToSpeaker)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }
        }
    }
}


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
                    Image(
                        painter = painterResource(dev.johnoreilly.confetti.shared.R.drawable.ic_person_black_24dp),
                        contentDescription = speaker.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        colorFilter = ColorFilter.tint(color = if (isSystemInDarkTheme()) Color.White else Color.Black)
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
