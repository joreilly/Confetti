@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.sessiondetails

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.flowlayout.FlowRow
import dev.johnoreilly.confetti.SessionDetailsViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import org.koin.androidx.compose.getViewModel

@Composable
fun SessionDetailsRoute(conference: String, sessionId: String, onBackClick: () -> Unit) {
    val viewModel: SessionDetailsViewModel = getViewModel<SessionDetailsViewModel>().apply {
        configure(conference, sessionId)
    }
    val session by viewModel.session.collectAsStateWithLifecycle()
    SessionDetailView(session, onBackClick)
}


@Composable
fun SessionDetailView(session: SessionDetails?, popBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { popBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            session?.let { session ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(state = scrollState)
                ) {

                    Text(text = session.title,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.size(16.dp))
                    Text(text = session.sessionDescription ?: "",
                        style = MaterialTheme.typography.bodyMedium)

                    if (session.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(16.dp))
                        FlowRow(crossAxisSpacing = 8.dp) {
                            session.tags.forEach { tag ->
                                Chip(tag)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                    session.speakers.forEach { speaker ->
                        SessionSpeakerInfo(speaker = speaker.speakerDetails,
                            onSocialLinkClick = { socialItem, _ ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(socialItem.url))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun Chip(name: String) {
    Surface(
        modifier = Modifier.padding(end = 10.dp),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(10.dp)
        )
    }
}