package dev.johnoreilly.confetti.wear.startup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import dev.johnoreilly.confetti.AppSettings.Companion.CONFERENCE_NOT_SET
import dev.johnoreilly.confetti.wear.AppUiState

// FIXME: use https://developer.android.com/develop/ui/views/launch/splash-screen#suspend-drawing to avoid the progressbar
@Composable
fun InitialLoadingRoute(
    navigateToConferences: () -> Unit,
    navigateToHome: (String) -> Unit,
    appUiState: AppUiState?
) {
    when (appUiState?.defaultConference) {
        null -> {
            // TODO splash?
        }
        CONFERENCE_NOT_SET -> {
            SideEffect {
                navigateToConferences()
            }
        }
        else -> {
            SideEffect {
                navigateToHome(appUiState.defaultConference)
            }
        }
    }
}