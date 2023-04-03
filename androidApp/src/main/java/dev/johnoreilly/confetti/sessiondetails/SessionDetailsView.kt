@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package dev.johnoreilly.confetti.sessiondetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.SessionDetailsViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.speakerdetails.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.utils.format
import org.koin.androidx.compose.getViewModel
import java.time.format.DateTimeFormatter

@Composable
fun SessionDetailsRoute(
    conference: String,
    sessionId: String,
    onBackClick: () -> Unit,
    onSpeakerClick: (key: SpeakerDetailsKey) -> Unit
) {
    val viewModel: SessionDetailsViewModel = getViewModel<SessionDetailsViewModel>().apply {
        configure(conference, sessionId)
    }
    val session by viewModel.session.collectAsStateWithLifecycle()
    val share = rememberShareDetails(session)
    SessionDetailView(
        session = session,
        popBack = onBackClick,
        share = share,
        onSpeakerClick = { speakerId ->
            onSpeakerClick(SpeakerDetailsKey(conference = conference, speakerId = speakerId))
        }
    )
}

@Composable
fun SessionDetailView(
    session: SessionDetails?,
    popBack: () -> Unit,
    share: () -> Unit,
    onSpeakerClick: (speakerId: String) -> Unit
) {
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
                ),
                actions = {
                    IconButton(onClick = { share() }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            session?.let { session ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(state = scrollState)
                ) {

                    Text(
                        text = session.title,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = session.sessionDescription ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (session.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(16.dp))
                        FlowRow {
                            session.tags.distinct().forEach { tag ->
                                Box(Modifier.padding(bottom = 8.dp)) {
                                    Chip(tag)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                    session.speakers.forEach { speaker ->
                        SessionSpeakerInfo(speaker = speaker.speakerDetails,
                            onSocialLinkClick = { socialItem, _ ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(socialItem.url))
                                context.startActivity(intent)
                            },
                            onSpeakerClick = onSpeakerClick
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

@Composable
private fun rememberShareDetails(details: SessionDetails?): () -> Unit {
    val context = LocalContext.current

    return remember(context, details) {
        // If details is null, there is nothing to share.
        if (details == null) return@remember {}

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM")
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"

            val room = details.room?.name ?: "Unknown"
            val date = dateFormatter.format(details.startsAt)
            val startsAt = timeFormatter.format(details.startsAt)
            val endsAt = timeFormatter.format(details.endsAt)
            val schedule = "$date $startsAt-$endsAt"
            val speakers = details
                .speakers
                .map { it.speakerDetails.name }
                .toString()
                .removeSurrounding(prefix = "[", suffix = "]")

            val text =
                """
                |Title: ${details.title}
                |Schedule: $schedule
                |Room: $room
                |Speaker: $speakers
                |---
                |Description: ${details.sessionDescription}
                """.trimMargin()
            putExtra(Intent.EXTRA_TEXT, text)
        }

        val launchIntent = Intent.createChooser(sendIntent, null)
        return@remember { context.startActivity(launchIntent) }
    }
}
