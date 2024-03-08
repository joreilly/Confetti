@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import conferenceDayMonthFormat
import dev.johnoreilly.confetti.GetConferencesQuery
import kotlinx.datetime.LocalDate



@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConferenceListView(conferenceListByYear: Map<Int, List<GetConferencesQuery.Conference>>, navigateToConference: (GetConferencesQuery.Conference) -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(horizontal = 32.dp),
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        text = "Confetti",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        }) {
        LazyColumn(Modifier.padding(it)) {
            conferenceListByYear.forEach { (year, conferenceList) ->
                stickyHeader {
                    Text(text = year.toString(),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge)
                }

                items(conferenceList) { conference ->
                    ConferenceCard(conference) { navigateToConference(conference) }
                }
            }
        }
    }
}


@Composable
fun ConferenceCard(conference: GetConferencesQuery.Conference, navigateToConference: (GetConferencesQuery.Conference) -> Unit) {
    ConferenceMaterialTheme(conference.themeColor) {
        Card(modifier = Modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable(onClick = {
                navigateToConference(conference)
            })
            .padding(8.dp)
            .fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(conference.name, style = MaterialTheme.typography.titleMedium)
                Text(getConferenceDatesString(conference.days), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}


private fun getConferenceDatesString(days: List<LocalDate>): String {
    var conferenceDatesString = ""
    if (days.size >= 1) {
        conferenceDatesString = days[0].conferenceDayMonthFormat()
    }
    if (days.size == 2) {
        conferenceDatesString += " - ${days[1].conferenceDayMonthFormat()}"
    }
    return conferenceDatesString
}
