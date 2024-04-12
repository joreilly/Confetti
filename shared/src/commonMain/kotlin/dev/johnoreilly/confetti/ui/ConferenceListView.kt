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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import conferenceDayMonthFormat
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import kotlinx.datetime.LocalDate



@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConferenceListView(component: ConferencesComponent) {
    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp) {
                MediumTopAppBar(
                    title = {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            text = "Confetti",
                            fontSize = 32.sp,
                        )
                    }
                )
            }
        }) {

        val uiState by component.uiState.subscribeAsState()

        when (val uiState1 = uiState) {
            ConferencesComponent.Error -> {} //ErrorView(component::refresh)
            ConferencesComponent.Loading -> LoadingView()

            is ConferencesComponent.Success -> {
                LazyColumn(Modifier.padding(it)) {
                    val conferenceListByYear = uiState1.conferenceListByYear
                    conferenceListByYear.keys.sortedDescending().forEach { year ->
                        val conferenceList = conferenceListByYear[year]
                        conferenceList?.let {
                            stickyHeader {
                                YearHeader(year.toString())
                            }

                            items(conferenceList) { conference ->
                                ConferenceCard(conference) {
                                    component.onConferenceClicked(conference)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ConferenceCard(
    conference: GetConferencesQuery.Conference,
    navigateToConference: (GetConferencesQuery.Conference) -> Unit
) {
    ConferenceMaterialTheme(conference.themeColor) {
        Card(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
                .clickable(onClick = {
                    navigateToConference(conference)
                })
                .fillMaxWidth()
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(conference.name, style = MaterialTheme.typography.titleMedium)
                Text(getConferenceDatesString(conference.days), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}


private fun getConferenceDatesString(days: List<LocalDate>): String {
    var conferenceDatesString = ""
    if (days.isNotEmpty()) {
        conferenceDatesString = days[0].conferenceDayMonthFormat()
    }
    if (days.size == 2) {
        conferenceDatesString += " - ${days[1].conferenceDayMonthFormat()}"
    }
    return conferenceDatesString
}

@Composable
fun YearHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
