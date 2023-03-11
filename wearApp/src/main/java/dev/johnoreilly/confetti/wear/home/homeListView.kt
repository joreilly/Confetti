@file:OptIn(
    ExperimentalHorologistComposeLayoutApi::class, ExperimentalHorologistBaseUiApi::class
)

package dev.johnoreilly.confetti.wear.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import com.google.android.horologist.base.ui.ExperimentalHorologistBaseUiApi
import com.google.android.horologist.base.ui.components.StandardChip
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.type.Session
import dev.johnoreilly.confetti.wear.sessions.SessionCard
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
fun HomeListView(
    uiState: HomeUiState,
    sessionSelected: (sessionId: String) -> Unit,
    daySelected: (sessionId: LocalDate) -> Unit,
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    columnState: ScalingLazyColumnState
) {
    val dayFormatter = remember { DateTimeFormatter.ofPattern("cccc") }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        when (uiState) {
            is HomeUiState.Success -> {
                val sessions = uiState.currentSessions
                if (sessions.isNotEmpty()) {
                    items(sessions) { session ->
                        SessionCard(session, sessionSelected)
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
                        onClick = { daySelected(date) }
                    )
                }
            }

            else -> {
                // TODO
            }
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
            uiState = HomeUiState.Success(
                conference = "wearcon",
                conferenceName = "WearableCon 2022",
                confDates = listOf(sessionTime.toLocalDate().toKotlinLocalDate()),
                currentSessions = listOf(
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
                        listOf(),
                        Session.type.name
                    )
                )
            ),
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            sessionSelected = {},
            onSettingsClick = {},
            onRefreshClick = {},
            daySelected = {},
        )
    }
}
