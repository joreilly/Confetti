@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.conferences

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.ui.ConferenceListView
import dev.johnoreilly.confetti.ui.ConfettiTheme
import dev.johnoreilly.confetti.ui.ErrorView
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.component.ConfettiBackground
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month


@Composable
fun ConferencesRoute(
    component: ConferencesComponent,
) {
    val uiState by component.uiState.subscribeAsState()

    when (val uiState1 = uiState) {
        ConferencesComponent.Error -> ErrorView(component::refresh)
        ConferencesComponent.Loading -> LoadingView()

        is ConferencesComponent.Success ->
            ConferenceListView(
                conferenceListByYear = uiState1.conferenceListByYear,
                navigateToConference = component::onConferenceClicked,
            )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConferencesViewPreview() {
    ConfettiTheme {
        ConfettiBackground {
            ConferenceListView(
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
