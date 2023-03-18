package dev.johnoreilly.confetti.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.AppSettings.Companion.CONFERENCE_NOT_SET
import dev.johnoreilly.confetti.AppViewModel
import dev.johnoreilly.confetti.conferences.navigation.ConferencesKey
import dev.johnoreilly.confetti.sessions.navigation.SessionsKey
import dev.johnoreilly.confetti.ui.LoadingView
import org.koin.androidx.compose.getViewModel

@Composable
fun SplashRoute(
    navigateToConferences: (ConferencesKey) -> Unit,
    navigateToSessions: (SessionsKey) -> Unit
) {
    val viewModel = getViewModel<AppViewModel>()

    val conference by viewModel.conference.collectAsStateWithLifecycle()
    when (val conference1 = conference) {
        null -> LoadingView()
        CONFERENCE_NOT_SET -> navigateToConferences(ConferencesKey)
        else -> navigateToSessions(SessionsKey(conference1))
    }
}