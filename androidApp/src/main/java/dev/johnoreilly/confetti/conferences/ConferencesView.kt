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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.Conference
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.ui.ConfettiTheme
import dev.johnoreilly.confetti.ui.component.ConfettiBackground
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
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(conference.name, style = MaterialTheme.typography.bodyLarge)
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
                    Conference("0", "Droidcon San Francisco 2022"),
                    Conference("1", "FrenchKit 2022"),
                    Conference("2", "Droidcon London 2022"),
                    Conference("3", "DevFest Ukraine 2023"),
                ),
                navigateToConference = {}
            )
        }
    }
}
