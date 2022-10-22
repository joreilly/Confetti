package dev.johnoreilly.confetti.conferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.Conference
import dev.johnoreilly.confetti.ConfettiViewModel
import org.koin.androidx.compose.getViewModel


@Composable
fun ConferencesRoute(
    navigateToConference: (String) -> Unit,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val conferenceList = viewModel.conferenceList
    ConferencesView(conferenceList) { conference ->
        viewModel.setConference(conference)
        navigateToConference(conference)
    }
}

@Composable
fun ConferencesView(conferenceList: List<Conference>, navigateToConference: (String) -> Unit) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Text("Choose Conference", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.size(16.dp))
        LazyColumn {
            items(conferenceList) { conference ->
                Row(modifier = Modifier.padding(8.dp).clickable(onClick = {
                    navigateToConference(conference.id)
                })) {
                    Text(conference.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}