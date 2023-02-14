@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.sessions

import androidx.activity.compose.ReportDrawn
import androidx.activity.compose.ReportDrawnAfter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import androidx.wear.compose.foundation.lazy.items
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.sessions.navigation.ConferenceDateKey
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun SessionListView(
    date: ConferenceDateKey,
    uiState: SessionsUiState,
    sessionSelected: (sessionId: SessionDetailsKey) -> Unit,
    columnState: ScalingLazyColumnState
) {
    when (uiState) {
        SessionsUiState.Loading -> CircularProgressIndicator()

        is SessionsUiState.Success -> {
            ReportDrawn()

            val sessions = uiState.sessionsByStartTimeList[uiState.confDates.indexOf(date.date)]
            DaySessionList(date, sessions, sessionSelected, columnState)
        }
    }
}

@Composable
private fun DaySessionList(
    date: ConferenceDateKey,
    sessions: Map<String, List<SessionDetails>>,
    sessionSelected: (sessionId: SessionDetailsKey) -> Unit,
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
                        Text("${dayFormatter.format(date.date.toJavaLocalDate())} $time")
                    } else {
                        Text(time)
                    }
                }
            }

            items(sessions) { session ->
                SessionView(date.conference, session, sessionSelected)
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
            date = ConferenceDateKey("wearablecon2022", date),
            uiState = SessionsUiState.Success(
                conference = "wearablecon2022",
                now = sessionTime.toKotlinLocalDateTime(),
                conferenceName = "WearableCon 2022",
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
                ),
                speakers = listOf()
            ),
            sessionSelected = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}
