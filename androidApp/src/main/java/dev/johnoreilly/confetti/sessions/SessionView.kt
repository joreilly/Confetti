@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.component.ConfettiTopAppBar
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.ui.component.ConfettiGradientBackground
import org.koin.androidx.compose.getViewModel
import java.util.*


@Composable
fun SessionsRoute(
    navigateToSession: (String) -> Unit,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val sessions by viewModel.sessions.collectAsState(emptyList())

    val tiemFormatter: (SessionDetails) -> String = {
        viewModel.getSessionTime(it)
    }

    SessionListView(sessions, navigateToSession, tiemFormatter)
}

@Composable
fun SessionListView(
    sessions: List<SessionDetails>,
    sessionSelected: (sessionId: String) -> Unit,
    tiemFormatter: (SessionDetails) -> String
) {

    ConfettiGradientBackground {
        Scaffold(
            topBar = {
                ConfettiTopAppBar(
                    titleRes = R.string.sessions,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (sessions.isNotEmpty()) {
                    LazyColumn {
                        items(sessions) { session ->
                            SessionView(session, sessionSelected, tiemFormatter)
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


@Composable
fun SessionView(
    session: SessionDetails,
    sessionSelected: (sessionId: String) -> Unit,
    tiemFormatter: (SessionDetails) -> String
) {

    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = { sessionSelected(session.id) }),
    ) {

        Row(modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically) {

            val timeString = tiemFormatter(session)
            Text(timeString, color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = session.title, style = TextStyle(fontSize = 18.sp))
            }

            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                val sessionSpeakerLocationText = getSessionSpeakerLocation(session)
                Text(sessionSpeakerLocationText,  style = TextStyle(fontSize = 14.sp))
            }
        }
    }
}

fun getSessionSpeakerLocation(session: SessionDetails): String {
    var text = session.speakers.joinToString(", ") { it.name }
    text += " / ${session.room?.name} / ${getLanguageInEmoji(session.language)}"
    return text
}

fun getLanguageInEmoji(language: String?): String {
    // TODO need to figure out how we want to generally handle languages
    return when (language) {
        "en-US" -> "\uD83C\uDDEC\uD83C\uDDE7"
        "fr" -> "\uD83C\uDDEB\uD83C\uDDF7"
        else -> ""
    }
}


