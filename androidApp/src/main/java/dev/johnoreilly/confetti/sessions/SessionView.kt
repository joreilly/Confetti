@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalLifecycleComposeApi::class
)

package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.DisplayFeature
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.component.ConfettiTopAppBar
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.sessiondetails.SessionDetailView
import dev.johnoreilly.confetti.ui.component.ConfettiGradientBackground
import dev.johnoreilly.confetti.ui.component.ConfettiTab
import dev.johnoreilly.confetti.ui.component.ConfettiTabRow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel



@Composable
fun SessionsRoute(
    isExpandedScreen: Boolean,
    displayFeatures: List<DisplayFeature>,
    navigateToSession: (String) -> Unit,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var session by remember { mutableStateOf<SessionDetails?>(null) }

    val timeFormatter: (SessionDetails) -> String = {
        viewModel.getSessionTime(it)
    }

    // TODO probably move most of this in to another composable
    if (isExpandedScreen) {
        TwoPane(
            first = {
                SessionListContent(uiState,
                    switchTab = {
                        viewModel.switchTab(it)
                    },
                    sessionSelected = { sessionId ->
                    coroutineScope.launch {
                        session = viewModel.getSession(sessionId)
                    }

                }, timeFormatter)
            },
            second = {
                SessionDetailView(session, {})
            },
            strategy =  { density, layoutDirection, layoutCoordinates ->
                HorizontalTwoPaneStrategy(
                    splitFraction = 0.25f
                ).calculateSplitResult(density, layoutDirection, layoutCoordinates)
            },
            displayFeatures = displayFeatures,
            modifier = Modifier.padding(8.dp)
        )
    } else {
        SessionListContent(uiState,
            switchTab = {
                viewModel.switchTab((it))
            },
            navigateToSession, timeFormatter)
    }
}

@Composable
fun SessionListContent(
    uiState: SessionsUiState,
    switchTab: (Int) -> Unit,
    sessionSelected: (sessionId: String) -> Unit,
    timeFormatter: (SessionDetails) -> String
) {

    ConfettiGradientBackground {
        Scaffold(
            topBar = {
                ConfettiTopAppBar(
                    titleRes = R.string.sessions,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {

                when (uiState) {
                    SessionsUiState.Loading ->
                        Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
                            CircularProgressIndicator()
                        }
                    is SessionsUiState.Success ->
                        Column {
                            ConfettiTabRow(selectedTabIndex = uiState.selectedDateIndex) {
                                uiState.confDates.forEachIndexed { index, date ->
                                    ConfettiTab(
                                        selected = index == uiState.selectedDateIndex,
                                        onClick = {
                                            switchTab(index)
                                        },
                                        text = { Text(text = date.toString()) }
                                    )
                                }
                            }
                            LazyColumn {
                                items(uiState.sessions) { session ->
                                    SessionView(session, sessionSelected, timeFormatter)
                                }
                            }
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

    var modifier = Modifier.fillMaxSize()
    if (!session.isBreak()) {
        modifier = modifier.clickable(onClick = {
            sessionSelected(session.id)
        })
    }
    Column(modifier) {

        Row(modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically) {

            val timeString = tiemFormatter(session)
            Text(timeString, color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
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
    var text = if (session.speakers.size > 0)
        session.speakers.joinToString(", ") { it.name } + " / "
    else
        ""
    text += "${session.room?.name} / ${getLanguageInEmoji(session.language)}"
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


