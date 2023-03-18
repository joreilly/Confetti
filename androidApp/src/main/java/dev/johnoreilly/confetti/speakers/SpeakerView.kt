@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.speakers

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.SpeakersUiState
import dev.johnoreilly.confetti.SpeakersViewModel
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.speakerdetails.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.ui.ConfettiAppState
import dev.johnoreilly.confetti.ui.ConfettiScaffold
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import org.koin.androidx.compose.getViewModel


@Composable
fun SpeakersRoute(
    conference: String,
    appState: ConfettiAppState,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    val viewModel: SpeakersViewModel = getViewModel<SpeakersViewModel>().apply {
        configure(conference = conference)
    }
    val uiState by viewModel.speakers.collectAsStateWithLifecycle()

    ConfettiScaffold(
        conference = conference,
        title = stringResource(R.string.speakers),
        appState = appState,
        onSwitchConference = onSwitchConference,
        onSignIn = onSignIn,
        onSignOut = onSignOut
    ) {
        when (val uiState1 = uiState) {
            is SpeakersUiState.Success -> {
                if (appState.isExpandedScreen) {
                    SpeakerGridView(uiState1.conference, uiState1.speakers, navigateToSpeaker)
                } else {
                    SpeakerListView(uiState1.conference, uiState1.speakers, navigateToSpeaker)
                }
            }

            is SpeakersUiState.Loading -> LoadingView()
            is SpeakersUiState.Error -> ErrorView {

            }
        }
    }

}


@Composable
fun SpeakerGridView(
    conference: String,
    speakers: List<SpeakerDetails>,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier.padding(16.dp),
        columns = GridCells.Adaptive(200.dp),

        // content padding
        contentPadding = PaddingValues(8.dp),
        content = {
            items(speakers.size) { index ->
                val speaker = speakers[index]
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (speaker.photoUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = speaker.photoUrl,
                            contentDescription = speaker.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    } else {
                        Spacer(modifier = Modifier.size(150.dp))
                    }

                    Text(
                        text = speaker.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )

                }
            }
        }
    )
}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SpeakerListView(
    conference: String,
    speakers: List<SpeakerDetails>,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit
) {
    Column {
        if (speakers.isNotEmpty()) {
            LazyColumn {
                items(speakers) { speaker ->
                    SpeakerView(conference, speaker, navigateToSpeaker)
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
fun SpeakerView(
    conference: String,
    speaker: SpeakerDetails,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { navigateToSpeaker(SpeakerDetailsKey(conference, speaker.id)) })
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (speaker.photoUrl?.isNotEmpty() == true) {
            AsyncImage(
                model = speaker.photoUrl,
                contentDescription = speaker.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
        } else {
            Spacer(modifier = Modifier.size(60.dp))
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(text = speaker.name, style = TextStyle(fontSize = 20.sp))
            Text(
                text = speaker.company ?: "",
                style = TextStyle(color = Color.DarkGray, fontSize = 14.sp)
            )
        }
    }
}

