@file:OptIn(
    ExperimentalPagerApi::class, ExperimentalPagerApi::class,
    ExperimentalHorologistComposeLayoutApi::class
)

package dev.johnoreilly.confetti.wear.sessions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.items
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.scrollAway
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.pager.PagerScreen
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.SessionsUiState

@Composable
fun SessionListView(
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    onSwitchConferenceSelected: () -> Unit,
    onRefresh: suspend (() -> Unit)
) {
    when (uiState) {
        SessionsUiState.Loading -> CircularProgressIndicator()

        is SessionsUiState.Success -> {
            PagerScreen(
                count = uiState.confDates.size,
            ) { page ->
                val columnState = ScalingLazyColumnDefaults.belowTimeText().create()

                Scaffold(
                    timeText = { TimeText(modifier = Modifier.scrollAway(columnState)) },
                    positionIndicator = { PositionIndicator(columnState.state) }
                ) {
                    val sessions = uiState.sessionsByStartTimeList[page]
                    DaySessionList(sessions, sessionSelected, columnState)
                }
            }
        }
    }
}

@Composable
private fun DaySessionList(
    sessions: Map<String, List<SessionDetails>>,
    sessionSelected: (sessionId: String) -> Unit,
    columnState: ScalingLazyColumnState
) {
    ScalingLazyColumn(
        columnState = columnState,
    ) {
        sessions.forEach {
            item {
                ListHeader {
                    Text(it.key)
                }
            }

            items(it.value) { session ->
                SessionView(session, sessionSelected)
            }
        }
    }
}
