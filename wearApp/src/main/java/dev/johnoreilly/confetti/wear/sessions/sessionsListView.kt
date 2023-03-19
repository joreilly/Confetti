@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.sessions

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.type.Session
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import org.koin.androidx.compose.getViewModel
import java.time.format.DateTimeFormatter

@Composable
fun SessionsRoute(
    navigateToSession: (SessionDetailsKey) -> Unit,
    columnState: ScalingLazyColumnState,
    viewModel: SessionsViewModel = getViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (!BuildConfig.DEBUG) {
        ReportDrawnWhen {
            uiState is SessionsUiState.Success
        }
    }

    SessionListView(
        uiState = uiState,
        sessionSelected = {
            navigateToSession(it)
        },
        columnState = columnState
    )
}

@Composable
fun SessionListView(
    uiState: SessionsUiState,
    sessionSelected: (SessionDetailsKey) -> Unit,
    columnState: ScalingLazyColumnState
) {
    val dayFormatter = remember { DateTimeFormatter.ofPattern("eeee H:mm") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("H:mm") }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        when (uiState) {
            is SessionsUiState.Success -> {
                val sessions = uiState.sessionsByTime

                sessions.forEachIndexed { index, sessionsAtTime ->
                    item {
                        ListHeader {
                            val time = sessionsAtTime.time.toJavaLocalDateTime()
                            if (index == 0) {
                                Text(dayFormatter.format(time))
                            } else {
                                Text(timeFormatter.format(time))
                            }
                        }
                    }

                    items(sessionsAtTime.sessions) { session ->
                        SessionCard(session) {
                            sessionSelected(
                                SessionDetailsKey(
                                    conference = uiState.conferenceDay.conference,
                                    sessionId = it
                                )
                            )
                        }
                    }
                }
            }

            else -> {
                // TODO
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun SessionListViewPreview() {
    val sessionTime = LocalDateTime(2022, 12, 25, 12, 30)

    ConfettiTheme {
        SessionListView(
            uiState = SessionsUiState.Success(
                ConferenceDayKey("wearconf", sessionTime.date),
                sessionsByTime = listOf(
                    SessionsUiState.SessionAtTime(
                        sessionTime,
                        listOf(
                            SessionDetails(
                                "1",
                                "Wear it's at",
                                "Talk",
                                sessionTime,
                                sessionTime,
                                "Be aWear of what's coming",
                                "en",
                                listOf(),
                                SessionDetails.Room("Main Hall"),
                                listOf(),
                                Session.type.name
                            )
                        )
                    )
                )
            ),
            sessionSelected = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}
