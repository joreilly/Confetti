package dev.johnoreilly.confetti.wear.sessions


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.items
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.toKotlinLocalDateTime

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
                val sessions = uiState.sessionsByStartTimeList.firstOrNull().orEmpty()

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
    ConfettiThemeFixed {
        SessionsScreen(
            uiState = SessionsUiState.Success(
                now = java.time.LocalDateTime.of(2022, 1, 1, 1, 1).toKotlinLocalDateTime(),
                conferenceName = "wearconf",
                venueLat = null,
                venueLon = null,
                confDates = listOf(),
                formattedConfDates = listOf(),
                sessionsByStartTimeList = listOf(
                    mapOf(
                        "14:00" to listOf(
                            TestFixtures.sessionDetails
                        )
                    )
                ),
                speakers = listOf(),
                rooms = listOf(),
                bookmarks = setOf(),
                isRefreshing = false,
                searchString = "",
                selectedSessionId = null,
            ),
            sessionSelected = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}