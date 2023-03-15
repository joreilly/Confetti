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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.ui.component.ConfettiTopAppBar
import org.koin.androidx.compose.getViewModel


@Composable
fun SpeakersRoute(
    isExpandedScreen: Boolean,
    navigateToSpeaker: (String) -> Unit,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val speakers by viewModel.speakers.collectAsState(emptyList())

    if (isExpandedScreen) {
        SpeakerGridView(speakers, navigateToSpeaker)
    } else {
        SpeakerListView(speakers, navigateToSpeaker)
    }
}


@Composable
fun SpeakerGridView(speakers: List<SpeakerDetails>, @Suppress("UNUSED_PARAMETER") navigateToSpeaker: (String) -> Unit) {
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
                            model =speaker.photoUrl,
                            contentDescription = speaker.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(150.dp).clip(RoundedCornerShape(16.dp))
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
fun SpeakerListView(speakers: List<SpeakerDetails>, navigateToSpeaker: (String) -> Unit) {
    Scaffold(
        topBar = {
            ConfettiTopAppBar(
                title = stringResource(R.string.speakers),
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

