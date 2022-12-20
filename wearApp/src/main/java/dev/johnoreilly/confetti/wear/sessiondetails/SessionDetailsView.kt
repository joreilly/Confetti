@file:OptIn(ExperimentalLifecycleComposeApi::class)

package dev.johnoreilly.confetti.wear.sessiondetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import dev.johnoreilly.confetti.fragment.SessionDetails
import org.koin.androidx.compose.getViewModel

@Composable
fun SessionDetailsRoute(
    onBackClick: () -> Unit,
    viewModel: SessionDetailsViewModel = getViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    SessionDetailView(session, onBackClick)
}

@Composable
fun SessionDetailView(session: SessionDetails?, popBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(modifier = Modifier) {
        session?.let { session ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(state = scrollState)
            ) {

                Text(
                    text = session.title,
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.title1
                )

                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = session.description ?: "",
                    style = MaterialTheme.typography.body2
                )

                if (session.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(16.dp))
//                    FlowRow(crossAxisSpacing = 8.dp) {
//                        session.tags.forEach { tag ->
//                            Chip(tag)
//                        }
//                    }
                }

                Spacer(modifier = Modifier.size(16.dp))
                session.speakers.forEach { speaker ->
                    SessionSpeakerInfo(speaker = speaker.speakerDetails,
                        onSocialLinkClick = { socialItem, speakerDetails ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(socialItem.link))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

