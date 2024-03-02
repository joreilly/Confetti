package dev.johnoreilly.confetti.wear.sessiondetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import dev.johnoreilly.confetti.decompose.SessionDetailsComponent

@Composable
fun SessionDetailsRoute(
    component: SessionDetailsComponent,
) {
    val uiState by component.uiState.subscribeAsState()
    SessionDetailView(
        uiState = uiState,
        navigateToSpeaker = { component.onSpeakerClicked(it) }
    )
}