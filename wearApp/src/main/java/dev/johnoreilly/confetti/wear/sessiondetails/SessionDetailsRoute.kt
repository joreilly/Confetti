package dev.johnoreilly.confetti.wear.sessiondetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import dev.johnoreilly.confetti.decompose.SessionDetailsComponent

@Composable
fun SessionDetailsRoute(
    component: SessionDetailsComponent,
    columnState: ScalingLazyColumnState = rememberColumnState(),
) {
    val uiState by component.uiState.subscribeAsState()
    ScreenScaffold(scrollState = columnState) {
        SessionDetailView(
            uiState = uiState,
            columnState = columnState,
            navigateToSpeaker = { component.onSpeakerClicked(it) }
        )
    }
}