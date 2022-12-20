@file:OptIn(ExperimentalLifecycleComposeApi::class)

package dev.johnoreilly.confetti.wear.sessions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Text
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.wear.ConfettiViewModel
import org.koin.androidx.compose.getViewModel


@Composable
fun SessionsRoute(
    navigateToSession: (String) -> Unit,
    onSwitchConferenceSelected: () -> Unit,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


        SessionListView(
            uiState,
            navigateToSession,
            onSwitchConferenceSelected,
            { viewModel.refresh() }
        )
}



@Composable
fun SessionView(
    session: SessionDetails,
    sessionSelected: (sessionId: String) -> Unit
) {

    var modifier = Modifier.fillMaxSize()
    if (!session.isBreak()) {
        modifier = modifier.clickable(onClick = {
            sessionSelected(session.id)
        })
    }
    Column(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = session.title, style = TextStyle(fontSize = 16.sp))
        }

        session.room?.let {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sessionSpeakerLocationText = getSessionSpeakerLocation(session)
                Text(
                    sessionSpeakerLocationText,
                    style = TextStyle(fontSize = 14.sp), fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun getSessionSpeakerLocation(session: SessionDetails): String {
    var text = if (session.speakers.isNotEmpty())
        session.speakers.joinToString(", ") { it.speakerDetails.name }
    else
        ""
    text += " (${session.room?.name})" // / ${getLanguageInEmoji(session.language)}"
    return text
}

fun getLanguageInEmoji(language: String?): String {
    // TODO need to figure out how we want to generally handle languages
    return when (language) {
        "en-US" -> "\uD83C\uDDEC\uD83C\uDDE7"
        "fr-FR" -> "\uD83C\uDDEB\uD83C\uDDF7"
        else -> ""
    }
}


