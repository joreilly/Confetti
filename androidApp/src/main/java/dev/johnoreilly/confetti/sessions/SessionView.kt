package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails


@Composable
fun SessionListView(
    viewModel: ConfettiViewModel,
    bottomBar: @Composable () -> Unit,
    sessionSelected: (session: SessionDetails) -> Unit
) {
    val sessions by viewModel.sessions.collectAsState(emptyList())

    val enabledLanguages by viewModel.enabledLanguages.collectAsState(emptySet())

    val filterFavoriteSessions by viewModel.filterFavoriteSessions.collectAsState(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sessions") },
                // TODO need to figure out how we want to generally handle languages
//            actions = { Filter(enabledLanguages, onLanguageChecked = { languageCode, checked ->
//                viewModel.onLanguageChecked(languageCode, checked)
//            })
//            }
                actions = {
                    IconButton(onClick = {
                        viewModel.onFavoriteFilterClick()
                    }) {
                        Icon(
                            imageVector = if (filterFavoriteSessions) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Favorites"
                        )
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (sessions.isNotEmpty()) {
                LazyColumn {
                    itemsIndexed(sessions) { index, session ->
                        SessionView(viewModel, session, sessionSelected)
                        if (index == sessions.size - 1) {
                            viewModel.fetchMoreSessions()
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun Filter(enabledLanguages: Set<String>, onLanguageChecked: (String, Boolean) -> Unit) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }


    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Filled.FilterList,
            contentDescription = "Filter"
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        val languages = listOf(
            LanguageDescriptor("French", "fr-FR"),
            LanguageDescriptor("English", "en-US"),
        )

        languages.forEach { language ->
            DropdownMenuItem(onClick = {
                expanded = false
            }) {
                val flagResourceId = context.resources.getIdentifier(
                    "flag_${language.displayName.lowercase()}",
                    "drawable",
                    context.packageName
                )
                Image(
                    painterResource(flagResourceId),
                    modifier = Modifier.size(20.dp),
                    contentDescription = language.displayName
                )

                Spacer(modifier = Modifier.size(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = language.displayName)
                }
                Checkbox(checked = enabledLanguages.contains(language.ietfCode),
                    onCheckedChange = { onLanguageChecked(language.ietfCode, it) })
            }
        }
    }
}

private class LanguageDescriptor(
    val displayName: String,
    val ietfCode: String
)

@Composable
fun SessionView(
    viewModel: ConfettiViewModel,
    session: SessionDetails,
    sessionSelected: (session: SessionDetails) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { sessionSelected(session) }),
    ) {

        Row(
            modifier = Modifier
                .background(color = Color(0xFFEEEEEE))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val timeString = viewModel.getSessionTime(session)
            Text(timeString, color = Color.Black)
        }

        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = session.title, style = TextStyle(fontSize = 18.sp, color = Color.Blue))
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sessionSpeakerLocationText = viewModel.getSessionSpeakerLocation(session)
                Text(sessionSpeakerLocationText, style = TextStyle(fontSize = 14.sp))
            }
        }
    }

    Divider()
}


@Composable
fun SessionDetailView(viewModel: ConfettiViewModel, sessionId: String, popBack: () -> Unit) {
    val scrollState = rememberScrollState()

    val session by viewModel.getSession(sessionId).collectAsState(null)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(session?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = { popBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            session?.let { session ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(state = scrollState)
                ) {

                    Text(
                        text = session.title,
                        style = TextStyle(color = Color.Blue, fontSize = 22.sp)
                    )

                    Spacer(modifier = Modifier.size(16.dp))
                    Text(text = session.description, style = TextStyle(fontSize = 16.sp))

                    Spacer(modifier = Modifier.size(16.dp))
                    Row {
                        session.tags.forEach { tag ->
                            //Text(tag)
                            Chip(tag)
                        }
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                    session.speakers.forEach { speaker ->
                        Text(speaker.name, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(speaker.bio)
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                    Button(onClick = {
                        viewModel.setSessionFavorite(session.id, !session.isFavorite)
                    }) {
                        Text(if (session.isFavorite) "Remove from favorites" else "Add to favorites")
                    }
                }
            }
        }
    }
}


@Composable
fun Chip(name: String = "Chip") {
    Surface(
        modifier = Modifier.padding(end = 10.dp),
        elevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colors.primary
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.body2,
            color = Color.White,
            modifier = Modifier.padding(10.dp)
        )
    }
}
