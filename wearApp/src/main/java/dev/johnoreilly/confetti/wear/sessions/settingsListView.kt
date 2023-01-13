@file:OptIn(
    ExperimentalPagerApi::class, ExperimentalPagerApi::class,
    ExperimentalHorologistComposeLayoutApi::class
)

package dev.johnoreilly.confetti.wear.sessions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
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
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun SessionListView(
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    onSettingsClick: () -> Unit,
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
                    DaySessionList(sessions, sessionSelected, onSettingsClick, columnState)
                }
            }
        }
    }
}

@Composable
private fun DaySessionList(
    sessions: Map<String, List<SessionDetails>>,
    sessionSelected: (sessionId: String) -> Unit,
    onSettingsClick: () -> Unit,
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

        item {
            Button(onClick = onSettingsClick) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun SessionListViewPreview() {
    val sessionTime = LocalDateTime.of(2022, 12, 25, 12, 30)
    val startInstant = sessionTime.toInstant(ZoneOffset.UTC).toKotlinInstant()

    ConfettiTheme {
        SessionListView(
            uiState = SessionsUiState.Success(
                "WearableCon 2022",
                confDates = listOf(sessionTime.toLocalDate().toKotlinLocalDate()),
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
            onSettingsClick = {}
        )
    }
}
