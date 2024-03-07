@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.conferences

import android.content.res.Configuration
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.materialkolor.rememberDynamicColorScheme
import conferenceDayMonthFormat
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.ui.ConfettiTheme
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.component.ConfettiBackground
import dev.johnoreilly.confetti.ui.component.ConfettiHeaderAndroid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import java.lang.Long

@Composable
fun ConferencesRoute(
    component: ConferencesComponent,
) {
    val uiState by component.uiState.subscribeAsState()

    when (val uiState1 = uiState) {
        ConferencesComponent.Error -> ErrorView(component::refresh)
        ConferencesComponent.Loading -> LoadingView()

        is ConferencesComponent.Success ->
            ConferencesView(
                conferenceListByYear = uiState1.conferenceListByYear,
                navigateToConference = component::onConferenceClicked,
            )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConferencesView(conferenceListByYear: Map<Int, List<GetConferencesQuery.Conference>>, navigateToConference: (GetConferencesQuery.Conference) -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(horizontal = 32.dp),
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        text = "Confetti"
                    )
                }
            )
        }) {
        LazyColumn(Modifier.padding(it)) {
            conferenceListByYear.forEach { (year, conferenceList) ->
                stickyHeader {
                    ConfettiHeaderAndroid(text = year.toString())
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


    var seedColor = Color(0xFFFFFFFF)
    try {
        seedColor = Color(Long.decode(conference.themeColor))
    } catch (e: Exception) {}

    val colorScheme = rememberDynamicColorScheme(seedColor, false)

    MaterialTheme(colorScheme = colorScheme) {

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

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConferencesViewPreview() {
    ConfettiTheme {
        ConfettiBackground {
            ConferencesView(
                conferenceListByYear = mapOf(2022 to listOf(
                    GetConferencesQuery.Conference(
                        id = "0",
                        timezone = "",
                        days = listOf(LocalDate(2022, Month.JUNE, 2)),
                        name = "Droidcon San Francisco 2022",
                        themeColor = "0xFF800000"
                    ),
                    GetConferencesQuery.Conference(
                        id = "1",
                        timezone = "",
                        days = listOf(LocalDate(2022, Month.SEPTEMBER, 29)),
                        name = "FrenchKit 2022",
                        themeColor = "0xFF800000"
                    ),
                    GetConferencesQuery.Conference(
                        id = "2",
                        timezone = "",
                        days = listOf(LocalDate(2022, Month.OCTOBER, 27)),
                        name = "Droidcon London 2022",
                        themeColor = "0xFF800000"
                    ),
                    GetConferencesQuery.Conference(
                        id = "3",
                        timezone = "",
                        days = listOf(LocalDate(2022, Month.JUNE, 14)),
                        name = "DevFest Ukraine 2022",
                        themeColor = "0xFF800000"
                    ),
                ))
            ) {}
        }
    }
}
