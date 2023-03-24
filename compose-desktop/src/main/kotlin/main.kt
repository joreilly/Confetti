import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.seiko.imageloader.rememberAsyncImagePainter
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.fullNameAndCompany
import dev.johnoreilly.confetti.sessionSpeakerLocation
import dev.johnoreilly.confetti.utils.JvmDateService
import kotlinx.datetime.TimeZone
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault


private val koin = initKoin().koin
private val dateService = JvmDateService()

fun main() = application {
    val windowState = rememberWindowState()

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Confetti"
    ) {
        MaterialTheme {
            MainLayout()
        }
    }
}

@Composable
fun MainLayout() {
    val repository = koin.get<ConfettiRepository>()
    val currentSession: MutableState<SessionDetails?> = remember { mutableStateOf(null) }

    val sessionList = remember { mutableStateOf<List<SessionDetails>?>(emptyList()) }
    val conference = "kotlinconf2023"

    LaunchedEffect(conference) {
        val conferenceData = repository.conferenceData(conference, FetchPolicy.CacheFirst)
        sessionList.value = conferenceData.data?.sessions?.nodes?.map { it.sessionDetails }
    }

    Row(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth(0.3f), contentAlignment = Alignment.Center) {
            sessionList.value?.let { sessionList ->
                SessionListView(sessionList) {
                    currentSession.value = it
                }
            }
        }
        SessionDetailView(currentSession.value)
    }
}


@Composable
fun SessionListView(sessionList: List<SessionDetails>, sessionSelected: (player: SessionDetails) -> Unit) {
    Box(modifier = Modifier
            .padding(3.dp)
            .background(color = Color.White)
            .clip(shape = RoundedCornerShape(3.dp))
    ) {
        LazyColumn {
            items(items = sessionList, itemContent = { session ->
                SessionView(session, sessionSelected)
            })
        }
    }
}


@Composable
fun SessionView(session: SessionDetails, sessionSelected: (session: SessionDetails) -> Unit) {
    Row(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp).clickable(onClick = { sessionSelected(session) })) {

        Column(modifier = Modifier.weight(1f)) {
            val sessionTime = getSessionTime(session, currentSystemDefault())
            Text(
                sessionTime,
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.body2
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = session.title, style = TextStyle(fontSize = 16.sp))
            }

            session.room?.let {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        session.sessionSpeakerLocation(),
                        style = TextStyle(fontSize = 14.sp), fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}



@Composable
fun SessionDetailView(session: SessionDetails?) {
    val scrollState = rememberScrollState()
    Column {
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
                    style = MaterialTheme.typography.h4
                )

                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = session.sessionDescription ?: "",
                    style = MaterialTheme.typography.body2
                )


                Spacer(modifier = Modifier.size(16.dp))
                session.speakers.forEach { speaker ->
                    SessionSpeakerInfo(speaker = speaker.speakerDetails)
                }
            }
        }
    }
}

@Composable
fun SessionSpeakerInfo(
    modifier: Modifier = Modifier,
    speaker: SpeakerDetails
) {
    Column(modifier.padding(top = 16.dp)) {
        Row {
            speaker.photoUrl?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    modifier = Modifier.size(64.dp), contentDescription = speaker.name
                )
            }

            Column(Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = speaker.fullNameAndCompany(),
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold
                )

                speaker.city?.let { city ->
                    Text(
                        text = city,
                        style = MaterialTheme.typography.body2
                    )
                }

                speaker.bio?.let { bio ->
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = bio,
                        style = MaterialTheme.typography.body2
                    )
                }

            }
        }
    }
}

private fun getSessionTime(session: SessionDetails, timeZone: TimeZone): String {
    return dateService.format(session.startsAt, timeZone, "MMM dd, HH:mm")
}
