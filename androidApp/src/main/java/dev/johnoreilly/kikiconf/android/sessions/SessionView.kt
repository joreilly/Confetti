package dev.johnoreilly.kikiconf.android.sessions

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import dev.johnoreilly.kikiconf.android.KikiConfViewModel
import dev.johnoreilly.kikiconf.fragment.SessionDetails

@Composable
fun SessionListView(viewModel: KikiConfViewModel, bottomBar: @Composable () -> Unit, sessionSelected: (session: SessionDetails) -> Unit) {
    val sessions by viewModel.sessions.collectAsState(emptyList())

    Scaffold(
        topBar = { TopAppBar (title = { Text("Sessions") } ) },
        bottomBar = bottomBar
    ) {
        if (sessions.isNotEmpty()) {
            LazyColumn {
                items(sessions) { session ->
                    SessionView(session, sessionSelected)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
                CircularProgressIndicator()
            }
        }
    }
}


@Composable
fun SessionView(session: SessionDetails, sessionSelected: (session: SessionDetails) -> Unit) {
    val context = LocalContext.current

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = { sessionSelected(session) })
        .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val flagResource = if (session.language == "French") "flag_fr" else "flag_uk"
        val flagResourceId = context.resources.getIdentifier(flagResource, "drawable", context.getPackageName())
        if (flagResourceId != 0) {
            Image(painterResource(flagResourceId), modifier = Modifier.size(32.dp), contentDescription = "French")
        }

        Spacer(modifier = Modifier.size(16.dp))


        Text(text = session.title, style = TextStyle(fontSize = 16.sp))
    }

    Divider()
}


@Composable
fun SessionDetailView(viewModel: KikiConfViewModel, sessionId: String, popBack: () -> Unit) {
    val scrollState = rememberScrollState()

    val session by produceState<SessionDetails?>(initialValue = null, sessionId) {
        value = viewModel.getSession(sessionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text( session?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = { popBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        session?.let { session ->
            Column(modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(state = scrollState)
            ) {

                Text(text = session.title, style = TextStyle(color = Color.Blue,fontSize = 22.sp))

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