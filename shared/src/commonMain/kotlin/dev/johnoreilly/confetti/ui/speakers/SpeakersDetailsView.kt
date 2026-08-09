package dev.johnoreilly.confetti.ui.speakers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import dev.johnoreilly.confetti.ui.component.FullScreenPhotoDialog
import coil3.compose.SubcomposeAsyncImage
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.sessions
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.icons.Person
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.avatarUrl
import dev.johnoreilly.confetti.preview.johnOreillySpeaker
import dev.johnoreilly.confetti.preview.martinBonninSpeaker
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.sessions.SocialIcon
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerDetailsView(
    conference: String,
    speaker: SpeakerDetails,
    navigateToSession: (id: String) -> Unit,
    popBack: () -> Unit,
    onSocialLinkClicked: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    var showFullScreenPhoto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { popBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .padding(contentPaddings),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val url = speaker.avatarUrl()
                    SubcomposeAsyncImage(
                        model = url,
                        contentDescription = speaker.name,
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        },
                        error = {
                            Icon(
                                imageVector = ConfettiIcons.Person,
                                contentDescription = speaker.name,
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(CircleShape),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .clickable { showFullScreenPhoto = true }
                    )

                    Spacer(modifier = Modifier.size(16.dp))

                    Text(
                        text = speaker.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    speaker.tagline?.let { tagline ->
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = tagline,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (speaker.socials.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            speaker.socials.forEach { socialsItem ->
                                SocialIcon(
                                    modifier = Modifier.size(24.dp),
                                    socialItem = socialsItem,
                                    onClick = { onSocialLinkClicked(socialsItem.url) }
                                )
                            }
                        }
                    }

                    speaker.bio?.let { bio ->
                        Spacer(modifier = Modifier.size(24.dp))
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            SpeakerTalks(
                modifier = Modifier.padding(vertical = 8.dp),
                sessions = speaker.sessions,
                navigateToSession = navigateToSession,
            )
        }
    }

    if (showFullScreenPhoto) {
        FullScreenPhotoDialog(
            photoUrl = speaker.avatarUrl(),
            contentDescription = speaker.name,
            onDismissRequest = { showFullScreenPhoto = false }
        )
    }
}

@Composable
fun SpeakerTalks(
    sessions: List<SpeakerDetails.Session>,
    navigateToSession: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(Modifier.fillMaxWidth()) {

        ConfettiHeader(icon = Icons.Filled.Event, text = stringResource(Res.string.sessions))

        Spacer(modifier = Modifier.size(8.dp))

        Column(modifier) {
            sessions.forEachIndexed { index, session ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navigateToSession(session.id) },
                    headlineContent = {
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
                if (index < sessions.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@MobilePreviews
@Composable
internal fun SpeakerDetailsViewLoadedPreview() {
    MaterialTheme {
        SpeakerDetailsView(
            conference = "kotlinconf2023",
            speaker = johnOreillySpeaker,
            navigateToSession = {},
            popBack = {},
            onSocialLinkClicked = {},
        )
    }
}

@Preview(name = "Different speaker", widthDp = 411, heightDp = 914, showBackground = true)
@Composable
internal fun SpeakerDetailsViewAlternateSpeakerPreview() {
    MaterialTheme {
        SpeakerDetailsView(
            conference = "kotlinconf2023",
            speaker = martinBonninSpeaker,
            navigateToSession = {},
            popBack = {},
            onSocialLinkClicked = {},
        )
    }
}
