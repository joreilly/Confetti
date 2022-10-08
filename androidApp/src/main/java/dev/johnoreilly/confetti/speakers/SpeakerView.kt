@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.speakers

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.ui.component.ConfettiGradientBackground
import dev.johnoreilly.confetti.ui.component.ConfettiTopAppBar
import org.koin.androidx.compose.getViewModel


@Composable
fun SpeakersRoute(navigateToSpeaker: (String) -> Unit, viewModel: ConfettiViewModel = getViewModel()) {
    val speakers by viewModel.speakers.collectAsState(emptyList())
    SpeakerListView(speakers, navigateToSpeaker)
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SpeakerListView(speakers: List<SpeakerDetails>, navigateToSpeaker: (String) -> Unit) {
    ConfettiGradientBackground {
        Scaffold(
            topBar = {
                ConfettiTopAppBar(
                    titleRes = R.string.speakers,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                if (speakers.isNotEmpty()) {
                    LazyColumn {
                        items(speakers) { speaker ->
                            SpeakerView(speaker, navigateToSpeaker)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun SpeakerView(speaker: SpeakerDetails, navigateToSpeaker: (String) -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = { navigateToSpeaker(speaker.id) })
        .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (speaker.photoUrl?.isNotEmpty() == true) {
            AsyncImage(
                model =speaker.photoUrl,
                contentDescription = speaker.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(60.dp).clip(CircleShape)
            )
        } else {
            Spacer(modifier = Modifier.size(60.dp))
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(text = speaker.name, style = TextStyle(fontSize = 20.sp))
            Text(text = speaker.company ?: "", style = TextStyle(color = Color.DarkGray, fontSize = 14.sp))
        }
    }
}

