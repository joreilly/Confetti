@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.speakerdetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.ui.component.SocialIcon
import org.koin.androidx.compose.getViewModel


@Composable
fun SpeakerDetailsRoute(onBackClick: () -> Unit, viewModel: SpeakerDetailsViewModel = getViewModel()) {
    val speaker by viewModel.speaker.collectAsStateWithLifecycle()
    SpeakerDetailsView(speaker, onBackClick)
}


@Composable
fun SpeakerDetailsView(speaker: SpeakerDetails?, popBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(PaddingValues(start = 16.dp, end = 16.dp)),
                        text = speaker?.name ?: "",
                        maxLines = 1, overflow = TextOverflow.Ellipsis
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
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            speaker?.let { speaker ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(state = scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    val imageUrl = speaker.photoUrl ?: ""
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = speaker.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                    Spacer(modifier = Modifier.size(24.dp))

                    Text(
                        text = speaker.bio ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.size(24.dp))
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
            }
        }
    }
}


