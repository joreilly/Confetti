@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.sessiondetails

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.decompose.SessionDetailsComponent
import dev.johnoreilly.confetti.decompose.SessionDetailsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionSpeakerChip
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import dev.johnoreilly.confetti.wear.ui.previews.WearSmallRoundDevicePreview
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SessionDetailsRoute(
    component: SessionDetailsComponent,
    columnState: ScalingLazyColumnState,
) {
    val uiState by component.uiState.subscribeAsState()
    SessionDetailView(
        uiState = uiState,
        columnState = columnState,
        navigateToSpeaker = { component.onSpeakerClicked(it) }
    )
}

@Composable
fun SessionDetailView(
    uiState: SessionDetailsUiState,
    columnState: ScalingLazyColumnState,
    navigateToSpeaker: (String) -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("eeee HH:mm") }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState
    ) {
        when (uiState) {
            is SessionDetailsUiState.Success -> {
                val session = uiState.sessionDetails
                val description = session.descriptionParagraphs()

                item {
                    SectionHeader(text = session.title)
                }

                item {
                    val time = remember(session.startsAt) {
                        timeFormatter.format(session.startsAt.toJavaLocalDateTime())
                    }
                    Text(time)
                }

                items(description) {
                    Text(text = it)
                }

                items(session.speakers) { speaker ->
                    SessionSpeakerChip(
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

//@WearSmallRoundDevicePreview
//@Composable
//fun SessionDetailsLongText() {
//    ConfettiTheme {
//        SessionDetailView(
//            sessionId = SessionDetailsKey(TestFixtures.sessionDetails.id, "1"),
//            uiState = QueryResult.Success(
//                SessionDetailsUiState(
//                    session = TestFixtures.sessionDetails,
//                    timeZone = TimeZone.UTC
//                )
//            ),
//            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
//        ) {}
//    }
//}
//
//@WearPreviewDevices
//@WearPreviewFontSizes
//@Composable
//fun SessionDetailsViewPreview() {
//    ConfettiTheme {
//        SessionDetailView(
//            sessionId = SessionDetailsKey(TestFixtures.sessionDetails.id, "1"),
//            uiState = QueryResult.Success(
//                SessionDetailsUiState(
//                    session = TestFixtures.sessionDetails,
//                    timeZone = TimeZone.UTC
//                )
//            ),
//            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
//        ) {}
//    }
//}

