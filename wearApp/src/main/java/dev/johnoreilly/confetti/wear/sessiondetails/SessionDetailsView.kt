@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.sessiondetails

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.type.Session
import dev.johnoreilly.confetti.utils.AndroidDateService
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionSpeakerChip
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import dev.johnoreilly.confetti.wear.ui.previews.WearSmallRoundDevicePreview
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.koin.androidx.compose.getViewModel

@Composable
fun SessionDetailsRoute(
    columnState: ScalingLazyColumnState,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit,
    viewModel: SessionDetailsViewModel = getViewModel()
) {
    val uiState by viewModel.session.collectAsStateWithLifecycle()
    SessionDetailView(
        uiState = uiState,
        columnState = columnState,
        navigateToSpeaker = navigateToSpeaker,
        formatter = {
            viewModel.formatter.format(
                it,
                (uiState as SessionDetailsUiState.Success).timeZone,
                "eeee HH:mm"
            )
        }
    )
}

@Composable
fun SessionDetailView(
    uiState: SessionDetailsUiState,
    columnState: ScalingLazyColumnState,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit,
    formatter: (LocalDateTime) -> String
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState
    ) {
        when (uiState) {
            is SessionDetailsUiState.Success -> {
                val session = uiState.session
                val description = session.descriptionParagraphs()

                item {
                    SectionHeader(text = session.title)
                }

                item {
                    val time = remember(session.startsAt) {
                        formatter(session.startsAt)
                    }
                    Text(time)
                }

                items(description) {
                    Text(text = it)
                }

                items(session.speakers) { speaker ->
                    SessionSpeakerChip(
                        conference = uiState.conference,
                        speaker = speaker.speakerDetails,
                        navigateToSpeaker = navigateToSpeaker
                    )
                }
            }

            else -> {}
        }
    }
}

private fun SessionDetails?.descriptionParagraphs(): List<String> =
    this?.sessionDescription?.split("\n+".toRegex()).orEmpty()

@WearSmallRoundDevicePreview
@Composable
fun SessionDetailsLongText() {
    val sessionTime = LocalDateTime(2022, 12, 25, 12, 30)

    ConfettiTheme {
        SessionDetailView(
            uiState = SessionDetailsUiState.Success(
                conference = "wearconf",
                sessionId = SessionDetailsKey("", ""),
                session = SessionDetails(
                    "1",
                    "This is a really long talk title that seems to go forever.",
                    "Talk",
                    sessionTime,
                    sessionTime,
                    "Be aWear of what's coming, don't walk, run to attend this session.",
                    "en",
                    listOf(),
                    SessionDetails.Room("Main Hall"),
                    listOf(),
                    Session.type.name
                ),
                timeZone = TimeZone.UTC
            ),
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            navigateToSpeaker = {},
            formatter = { AndroidDateService().format(it, TimeZone.UTC, "eeee HH:mm") }
        )
    }
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun SessionDetailsViewPreview() {
    ConfettiTheme {
        SessionDetailView(SessionDetailsUiState.Success(
            conference = TestFixtures.kotlinConf2023.id,
            sessionId = SessionDetailsKey("", ""),
            session = TestFixtures.sessionDetails,
            timeZone = TimeZone.UTC
        ),
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            navigateToSpeaker = {},
            formatter = { AndroidDateService().format(it, TimeZone.UTC, "eeee HH:mm") }
        )
    }
}

