package dev.johnoreilly.confetti.initial_loading

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.AppSettings.Companion.CONFERENCE_NOT_SET
import dev.johnoreilly.confetti.AppViewModel
import dev.johnoreilly.confetti.conferences.navigation.ConferencesKey
import dev.johnoreilly.confetti.sessions.navigation.SessionsKey
import dev.johnoreilly.confetti.ui.LoadingView
import org.koin.androidx.compose.getViewModel

// FIXME: use https://developer.android.com/develop/ui/views/launch/splash-screen#suspend-drawing to avoid the progressbar
@Composable
fun InitialLoadingRoute(
    navigateToConferences: (ConferencesKey) -> Unit,
    navigateToSessions: (SessionsKey) -> Unit
) {
    val viewModel = getViewModel<AppViewModel>()

    val conference by viewModel.conference.collectAsStateWithLifecycle()
    when (val conference1 = conference) {
        null -> LoadingView()
        CONFERENCE_NOT_SET -> {
            SideEffect {
                navigateToConferences(ConferencesKey)
            }
        }
        else -> {
            SideEffect {
                navigateToSessions(SessionsKey(conference1))
            }
        }
    }
}