package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.sessions
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerDetailsView(
    conference: String,
    speaker: SpeakerDetails,
    navigateToSession: (id: String) -> Unit,
    popBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    SelectionContainer {
                        Text(
                            modifier = Modifier.padding(PaddingValues(start = 16.dp, end = 16.dp)),
                            text = speaker.name,
                            textAlign = TextAlign.Center
                        )
                    }
                },
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
                    speaker.tagline?.let { city ->
                        Text(
                            text = city,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.size(16.dp))

                    val url = "https://confetti-app.dev/images/avatar/${conference}/${speaker.id}"
                    AsyncImage(
                        model = url,
                        contentDescription = speaker.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )

                    Spacer(modifier = Modifier.size(24.dp))

                    speaker.bio?.let { bio ->
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.size(16.dp))
                    }

                    Row(
                        Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        speaker.socials.forEach { socialsItem ->
                            SocialIcon(
                                modifier = Modifier.size(24.dp),
                                socialItem = socialsItem,
                                onClick = {
//                                    runCatching {
//                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(socialsItem.url))
//                                        context.startActivity(intent)
//                                    }.getOrElse { error ->
//                                        error.printStackTrace()
//                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            SpeakerTalks(
                modifier = Modifier.padding(contentPaddings),
                sessions = speaker.sessions,
                navigateToSession = navigateToSession,
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SpeakerTalks(
    sessions: List<SpeakerDetails.Session>,
    navigateToSession: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(Modifier.fillMaxWidth()) {

        ConfettiHeader(icon = Icons.Filled.Person, text = stringResource(Res.string.sessions))

        Spacer(modifier = Modifier.size(8.dp))

        Column(modifier) {
            sessions.forEach { session ->
                Row(
                    Modifier
                        .padding()
                        .fillMaxWidth()
                        .clickable {
                            navigateToSession(session.id)
                        }
                        .padding(vertical = 8.dp)) {
                    Text(session.title, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
