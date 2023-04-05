@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.speakerdetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.johnoreilly.confetti.SpeakerDetailsViewModel
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.speakerdetails.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.component.SocialIcon
import org.koin.androidx.compose.getViewModel


@Composable
internal fun SpeakerDetailsRoute(
    conference: String,
    speakerDetailsKey: SpeakerDetailsKey,
    navigateToSession: (SessionDetailsKey) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: SpeakerDetailsViewModel = getViewModel<SpeakerDetailsViewModel>().apply {
        configure(speakerDetailsKey.conference, speakerDetailsKey.speakerId)
    }

    val uiState by viewModel.speaker.collectAsStateWithLifecycle()

    when (val uiState1 = uiState) {
        is SpeakerDetailsViewModel.Loading -> LoadingView()
        is SpeakerDetailsViewModel.Error -> ErrorView()
        is SpeakerDetailsViewModel.Success -> SpeakerDetailsView(
            conference,
            uiState1.details,
            navigateToSession,
            onBackClick
        )
    }

}


@Composable
fun SpeakerDetailsView(
    conference: String,
    speaker: SpeakerDetails,
    navigateToSession: (SessionDetailsKey) -> Unit,
    popBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(PaddingValues(start = 16.dp, end = 16.dp)),
                        text = speaker.name,
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { popBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
    ) { innerPadding ->
        val contentPaddings = remember { PaddingValues(horizontal = 16.dp, vertical = 8.dp) }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(state = scrollState),
            ) {
            Column(
                modifier = Modifier
                    .padding(contentPaddings),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                speaker.tagline?.let { city ->
                    Text(
                        text = city,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))

                val imageUrl = speaker.photoUrl ?: ""
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = speaker.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
                Spacer(modifier = Modifier.size(24.dp))

                Text(
                    text = speaker.bio ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.size(16.dp))

                Row(
                    Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    speaker.socials.forEach { socialsItem ->
                        SocialIcon(
                            modifier = Modifier.size(24.dp),
                            socialItem = socialsItem,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(socialsItem.url))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            SpeakerTalks(
                modifier = Modifier.padding(contentPaddings),
                conference = conference,
                sessions = speaker.sessions,
                navigateToSession = navigateToSession,
            )
        }
    }
}

@Composable
fun SpeakerTalks(
    conference: String,
    sessions: List<SpeakerDetails.Session>,
    navigateToSession: (SessionDetailsKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(Modifier.fillMaxWidth()) {

        ConfettiHeader(icon = Icons.Filled.Event, text = "Sessions")

        Spacer(modifier = Modifier.size(8.dp))

        Column(modifier) {
            sessions.forEach { session ->
                Row(
                    Modifier
                        .padding()
                        .fillMaxWidth()
                        .clickable {
                            navigateToSession(SessionDetailsKey(conference, session.id))
                        }
                        .padding(vertical = 8.dp)) {
                    Text(session.title, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
