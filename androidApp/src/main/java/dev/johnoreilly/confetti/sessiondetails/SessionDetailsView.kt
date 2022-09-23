@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)

package dev.johnoreilly.confetti.sessiondetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.ui.component.ConfettiGradientBackground
import org.koin.androidx.compose.getViewModel

@Composable
fun SessionDetailsRoute(onBackClick: () -> Unit, viewModel: SessionDetailsViewModel = getViewModel()) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    SessionDetailView(session, onBackClick)
}


@Composable
fun SessionDetailView(session: SessionDetails?, popBack: () -> Unit) {
    val scrollState = rememberScrollState()

    ConfettiGradientBackground {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            modifier = Modifier.padding(PaddingValues(start = 16.dp, end = 16.dp)),
                            text = session?.title ?: "",
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { popBack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
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
                session?.let { session ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(state = scrollState)
                    ) {

                        Text(
                            text = session.title,
                            style = TextStyle(color = Color.Blue, fontSize = 22.sp)
                        )

                        Spacer(modifier = Modifier.size(16.dp))
                        Text(text = session.description ?: "", style = TextStyle(fontSize = 16.sp))

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
                            Text(speaker.bio ?: "")
                        }
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
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.padding(10.dp)
        )
    }
}