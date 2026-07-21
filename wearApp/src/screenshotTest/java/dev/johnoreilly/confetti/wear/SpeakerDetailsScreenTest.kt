@file:OptIn(ExperimentalCoilApi::class)

package dev.johnoreilly.confetti.wear

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewLargeRound
import coil.annotation.ExperimentalCoilApi
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.speakerdetails.SpeakerDetailsView

@WearPreviewLargeRound
@Composable
fun SpeakerDetailsScreen() {
    TestScaffold {
        SpeakerDetailsView(
            uiState = SpeakerDetailsUiState.Success(
                conference = TestFixtures.conference,
                details = TestFixtures.JohnOreilly.speakerDetails,
            )
        )
    }
}
