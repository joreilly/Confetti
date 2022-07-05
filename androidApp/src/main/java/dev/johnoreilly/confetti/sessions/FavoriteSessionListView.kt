package dev.johnoreilly.confetti.sessions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails


@Composable
fun FavoriteSessionListView(
    viewModel: ConfettiViewModel,
    bottomBar: @Composable () -> Unit,
    sessionSelected: (session: SessionDetails) -> Unit
) {
    val sessions by viewModel.favoriteSessions.collectAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Sessions") },
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
                            viewModel.fetchMoreFavoriteSessions()
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

