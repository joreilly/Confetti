@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.sessions

import android.os.Build
import androidx.activity.compose.ReportDrawn
import androidx.activity.compose.ReportDrawnAfter
import androidx.annotation.RequiresApi
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
    date: LocalDate,
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    columnState: ScalingLazyColumnState
) {
    when (uiState) {
        SessionsUiState.Loading -> CircularProgressIndicator()

        is SessionsUiState.Success -> {
            ReportDrawn()

            val sessions = uiState.sessionsByStartTimeList[uiState.confDates.indexOf(date)]
            DaySessionList(date, sessions, sessionSelected, columnState)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
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

