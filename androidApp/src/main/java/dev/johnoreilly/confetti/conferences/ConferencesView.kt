package dev.johnoreilly.confetti.conferences

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.ConferencesViewModel
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.ui.ConfettiTheme
import dev.johnoreilly.confetti.ui.component.ConfettiBackground
import org.koin.androidx.compose.getViewModel

@Composable
fun ConferencesRoute(
    navigateToConference: (String) -> Unit,
) {
    val viewModel = getViewModel<ConferencesViewModel>()
    val conferenceList by viewModel.conferenceList.collectAsStateWithLifecycle()
    ConferencesView(conferenceList) { conference ->
        navigateToConference(conference)
    }
}

@Composable
fun ConferencesView(conferenceList: List<GetConferencesQuery.Conference>, navigateToConference: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Choose Conference", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.size(24.dp))
        LazyColumn {
            items(conferenceList) { conference ->
                Row(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .clickable(onClick = {
                            navigateToConference(conference.id)
                        })
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(conference.name, modifier = Modifier.weight(1.0f), style = MaterialTheme.typography.bodyLarge)
                    Text(conference.days[0].toString(), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConferencesViewPreview() {
    ConfettiTheme {
        ConfettiBackground {
            ConferencesView(
                conferenceList = listOf(
                    GetConferencesQuery.Conference("0", emptyList(), "Droidcon San Francisco 2022"),
                    GetConferencesQuery.Conference("1", emptyList(), "FrenchKit 2022"),
                    GetConferencesQuery.Conference("2", emptyList(), "Droidcon London 2022"),
                    GetConferencesQuery.Conference("3", emptyList(), "DevFest Ukraine 2023"),
                ),
                navigateToConference = {}
            )
        }
    }
}
