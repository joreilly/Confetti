package dev.johnoreilly.confetti.wear.sessions

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.decompose.SessionsUiState

@Composable
fun SessionsRoute(
    component: ConferenceSessionsComponent,
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
    )
}

