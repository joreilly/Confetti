@file:OptIn(ExperimentalCoilApi::class)

package dev.johnoreilly.confetti.wear

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewLargeRound
import coil.annotation.ExperimentalCoilApi
import dev.johnoreilly.confetti.decompose.SessionDetailsUiState
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailView

@WearPreviewLargeRound
@Composable
fun SessionDetailsScreen() {
    TestScaffold {
        SessionDetailView(
            uiState = SessionDetailsUiState.Success(
                conference = TestFixtures.conference,
                sessionDetails = TestFixtures.sessionDetails
            ),
            navigateToSpeaker = {}
        )
    }
}
