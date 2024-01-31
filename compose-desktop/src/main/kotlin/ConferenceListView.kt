import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetConferencesQuery

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConferenceListView(onConferenceSelected: (GetConferencesQuery.Conference) -> Unit) {
    val repository = koin.get<ConfettiRepository>()

    var conferenceListByYear by remember { mutableStateOf<Map<Int, List<GetConferencesQuery.Conference>>>(emptyMap()) }

    LaunchedEffect(repository) {
        repository.conferences(FetchPolicy.CacheFirst).data?.conferences?.let {
            conferenceListByYear = it.groupBy { it.days[0].year }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(horizontal = 32.dp),
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        text = "Conferences"
                    )
                }
            )
        }) {
        LazyColumn(Modifier.padding(it)) {
            conferenceListByYear.forEach { (year, conferenceList) ->
                stickyHeader {
                    ConfettiHeader(text = year.toString())
                }

                items(conferenceList) { conference ->
                    Row(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(8.dp))
                            .clickable(onClick = {
                                onConferenceSelected(conference)
                            })
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(conference.name, modifier = Modifier.weight(1.0f).padding(end = 16.dp), style = MaterialTheme.typography.bodyLarge)
                        Text(conference.days[0].toString(), style = MaterialTheme.typography.bodyLarge)
                    }
                }

            }
        }
    }

}