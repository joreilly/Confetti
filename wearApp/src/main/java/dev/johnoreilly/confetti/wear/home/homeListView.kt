@file:OptIn(
    ExperimentalHorologistComposeLayoutApi::class, ExperimentalHorologistBaseUiApi::class
)

package dev.johnoreilly.confetti.wear.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import androidx.wear.compose.foundation.lazy.items
import com.google.android.horologist.base.ui.ExperimentalHorologistBaseUiApi
import com.google.android.horologist.base.ui.components.StandardChip
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.sessions.SessionView
import dev.johnoreilly.confetti.wear.sessions.navigation.ConferenceDateKey
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun HomeListView(
    uiState: SessionsUiState,
    sessionSelected: (sessionId: SessionDetailsKey) -> Unit,
    daySelected: (date: ConferenceDateKey) -> Unit,
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    columnState: ScalingLazyColumnState
) {
    when (uiState) {
        SessionsUiState.Loading -> {
            CircularProgressIndicator(modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center))
        }

        is SessionsUiState.Success -> HomeList(uiState, sessionSelected, daySelected,
                                            onSettingsClick, onRefreshClick, columnState)
    }
}

@Composable
private fun HomeList(
    uiState: SessionsUiState.Success,
    sessionSelected: (SessionDetailsKey) -> Unit,
    daySelected: (date: ConferenceDateKey) -> Unit,
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    columnState: ScalingLazyColumnState
) {
    val sessions = uiState.currentSessions(uiState.now)

    // Monday
    val dayFormatter = remember { DateTimeFormatter.ofPattern("cccc") }

    ScalingLazyColumn(
        columnState = columnState,
    ) {
        if (sessions != null) {
            sessions.forEachIndexed { index, (time, sessions) ->
                item {
                    ListHeader {
                        if (index == 0) {
                            Text("${dayFormatter.format(uiState.now.toJavaLocalDateTime())}\n$time")
                        } else {
                            Text(time)
                        }
                    }
                }

                items(sessions) { session ->
                    SessionView(uiState.conference, session, sessionSelected)
                }
            }
        } else {
            item {
                ListHeader {
                    Text("No sessions today")
                }
            }
        }

        item {
            ListHeader {
                Text("Conference Days")
            }
        }

        items(uiState.confDates.size) {
            // TODO format date
            val date = uiState.confDates[it]
            StandardChip(
                label = dayFormatter.format(date.toJavaLocalDate()),
                onClick = { daySelected(ConferenceDateKey(uiState.conference, date)) }
            )
        }

        item {
            Button(onClick = onSettingsClick) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        item {
            Button(onClick = onRefreshClick) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

    }
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun HomeListViewPreview() {
    val sessionTime = LocalDateTime.of(2022, 12, 25, 12, 30)
    val startInstant = sessionTime.toInstant(ZoneOffset.UTC).toKotlinInstant()

    ConfettiTheme {
        HomeListView(
            uiState = SessionsUiState.Success(
                conference = "wearablecon2022",
                now = sessionTime.toKotlinLocalDateTime(),
                conferenceName = "WearableCon 2022",
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
                ),
                speakers = listOf()
            ),
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            sessionSelected = {},
            onSettingsClick = {},
            onRefreshClick = {},
            daySelected = {}
        )
    }
}
