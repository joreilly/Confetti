package dev.johnoreilly.kikiconf.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import dev.johnoreilly.kikiconf.GetSessionsQuery
import dev.johnoreilly.kikiconf.KikiConfRepository


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainLayout()
        }
    }
}



@Composable
fun MainLayout() {
    val repo = remember { KikiConfRepository() }
    val sessionList by produceState(initialValue = emptyList<GetSessionsQuery.Session>(), repo) {
        value = repo.getSessions()
    }

    Scaffold(topBar = { TopAppBar (title = { Text("Sessions") } ) }) {
        Sessionist(sessionList)
    }

}

@Composable
fun Sessionist(sessionList: List<GetSessionsQuery.Session>) {
    LazyColumn {
        items(sessionList) { session ->
            SessionView(session)
        }
    }
}


@Composable
fun SessionView(session: GetSessionsQuery.Session) {
    ListItem(
        text = { Text(session.title, style = MaterialTheme.typography.h6) }
    )
    Divider()
}

