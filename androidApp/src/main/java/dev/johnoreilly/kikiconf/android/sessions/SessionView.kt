package dev.johnoreilly.kikiconf.android.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.johnoreilly.kikiconf.android.KikiConfViewModel
import dev.johnoreilly.kikiconf.model.Session

@Composable
fun SessionListView(viewModel: KikiConfViewModel, sessionSelected: (session: Session) -> Unit) {
    val sessions by viewModel.sessions.collectAsState()

    LazyColumn {
        items(sessions) { session ->
            SessionView(session, sessionSelected)
        }
    }
}


@Composable
fun SessionView(session: Session, sessionSelected: (session: Session) -> Unit) {

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = { sessionSelected(session) })
        .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = session.title, style = TextStyle(fontSize = 20.sp))
    }

    Divider()
}


@Composable
fun SessionDetailView(viewModel: KikiConfViewModel, sessionId: String, popBack: () -> Unit) {

    val session by produceState<Session?>(initialValue = null, sessionId) {
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
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                Text(text = session.title, style = TextStyle(color = Color.DarkGray,fontSize = 22.sp))

                Spacer(modifier = Modifier.size(12.dp))

                Text(text = session.description, style = TextStyle(fontSize = 16.sp))

                Spacer(modifier = Modifier.size(12.dp))

                session.tags.forEach {
                    Text(it)
                }

            }
        }
    }
}
