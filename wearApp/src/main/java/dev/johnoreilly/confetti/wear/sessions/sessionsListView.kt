@file:OptIn(
    ExperimentalPagerApi::class, ExperimentalPagerApi::class,
    ExperimentalHorologistComposeLayoutApi::class
)

package dev.johnoreilly.confetti.wear.sessions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.SessionsUiState
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun SessionListView(
    date: LocalDate,
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
) {
    when (uiState) {
        SessionsUiState.Loading -> CircularProgressIndicator()

        is SessionsUiState.Success -> {
            val columnState = ScalingLazyColumnDefaults.belowTimeText().create()

            val sessions = uiState.sessionsByStartTimeList[uiState.confDates.indexOf(date)]
            DaySessionList(date, sessions, sessionSelected, columnState)
        }
    }
}

@Composable
private fun DaySessionList(
    date: LocalDate,
    sessions: Map<String, List<SessionDetails>>,
    sessionSelected: (sessionId: String) -> Unit,
    columnState: ScalingLazyColumnState
) {
    // Monday
    val dayFormatter = remember { DateTimeFormatter.ofPattern("cccc") }

    ScalingLazyColumn(
        columnState = columnState,
    ) {
        sessions.entries.forEachIndexed { index, (time, sessions) ->
            item {
                ListHeader {
                    if (index == 0) {
                        Text("${dayFormatter.format(date.toJavaLocalDate())} $time")
                    } else {
                        Text(time)
                    }
                }
            }

            items(sessions) { session ->
                SessionView(session, sessionSelected)
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun SessionListViewPreview() {
    val sessionTime = LocalDateTime.of(2022, 12, 25, 12, 30)
    val startInstant = sessionTime.toInstant(ZoneOffset.UTC).toKotlinInstant()

    ConfettiTheme {
        val date = sessionTime.toLocalDate().toKotlinLocalDate()
        SessionListView(
            date = date,
            uiState = SessionsUiState.Success(
                "WearableCon 2022",
                confDates = listOf(date),
                rooms = listOf(),
                sessionsByStartTimeList = listOf(
                    mapOf(
                        "12:30" to listOf(
                            SessionDetails(
                                "1",
                                "Wear it's at",
                                "Talk",
                                startInstant,
                                startInstant,
                                "Be aWear of what's coming",
                                "en",
                                listOf(),
                                SessionDetails.Room("Main Hall"),
                                listOf()
                            )
                        )
                    )
                )
            ),
            sessionSelected = {},
        )
    }
}
