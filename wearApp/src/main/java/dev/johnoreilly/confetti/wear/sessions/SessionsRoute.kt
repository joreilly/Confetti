@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.sessions

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.items
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime

@Composable
fun SessionsRoute(
    component: ConferenceSessionsComponent,
    columnState: ScalingLazyColumnState
) {
    val uiState by component.uiState.subscribeAsState()

    if (!BuildConfig.DEBUG) {
        ReportDrawnWhen {
            uiState is SessionsUiState.Success
        }
    }

    SessionsScreen(
        uiState = uiState,
        sessionSelected = component::onSessionClicked,
        columnState = columnState
    )
}

@Composable
fun SessionsScreen(
    uiState: SessionsUiState,
    sessionSelected: (sessionId: String) -> Unit,
    columnState: ScalingLazyColumnState
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        when (uiState) {
            is SessionsUiState.Success -> {
                val sessions = uiState.sessionsByStartTimeList.first()

                sessions.forEach { (time, sessionsAtTime) ->
                    item {
                        SectionHeader(time)
                    }

                    items(sessionsAtTime) { session ->
                        SessionCard(
                            session,
                            sessionSelected = {
                                sessionSelected(it)
                            },
                            uiState.now
                        )
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

    ConfettiThemeFixed {
        SessionsScreen(
            uiState = SessionsUiState.Success(
                now = java.time.LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime(),
                conferenceName = "wearconf",
                venueLat = null,
                venueLon = null,
                confDates = listOf(),
                formattedConfDates = listOf(),
                sessionsByStartTimeList = listOf(),
                speakers = listOf(),
                rooms = listOf(),
                bookmarks = setOf(),
                isRefreshing = false,
                searchString = "",
                selectedSessionId = null,

//                    ConferenceDayKey("wearconf", sessionTime.date),
//                    sessionsByTime = listOf(
//                        SessionAtTime(
//                            sessionTime,
//                            listOf(
//                                SessionDetails(
//                                    "1",
//                                    "Wear it's at",
//                                    "Talk",
//                                    sessionTime,
//                                    sessionTime,
//                                    "Be aWear of what's coming",
//                                    "en",
//                                    listOf(),
//                                    SessionDetails.Room("Main Hall"),
//                                    listOf(),
//                                    Session.type.name
//                                )
//                            )
//                        )
//                    ),
//                    now =
            ),
            sessionSelected = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}
