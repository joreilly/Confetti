package dev.johnoreilly.confetti.wear.conferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import dev.johnoreilly.confetti.Conference
import dev.johnoreilly.confetti.wear.ConfettiViewModel
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
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
    )
    {
        item {
            Text("Choose Conference", style = MaterialTheme.typography.title1)
        }
        items(conferenceList) { conference ->
            Text(
                text = conference.name,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.clickable(onClick = {
                    navigateToConference(conference.id)
                })
            )
        }
    }
}