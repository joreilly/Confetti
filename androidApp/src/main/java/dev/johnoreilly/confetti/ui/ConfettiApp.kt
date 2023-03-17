package dev.johnoreilly.confetti.ui


import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.window.layout.DisplayFeature
import dev.johnoreilly.confetti.AppSettings.Companion.CONFERENCE_NOT_SET
import dev.johnoreilly.confetti.AppViewModel
import dev.johnoreilly.confetti.account.navigation.SignInDestination
import dev.johnoreilly.confetti.account.navigation.signInGraph
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.conferences.navigation.ConferencesDestination
import dev.johnoreilly.confetti.conferences.navigation.conferencesGraph
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.sessiondetails.navigation.sessionDetailsGraph
import dev.johnoreilly.confetti.sessions.navigation.SessionsDestination
import dev.johnoreilly.confetti.sessions.navigation.sessionsGraph
import dev.johnoreilly.confetti.speakerdetails.navigation.SpeakerDetailsDestination
import dev.johnoreilly.confetti.speakerdetails.navigation.speakerDetailsGraph
import dev.johnoreilly.confetti.speakers.navigation.speakersGraph
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun ConfettiApp(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
) {
    val appState: ConfettiAppState = rememberConfettiAppState(
        windowSizeClass,
        displayFeatures,
        navController
    )

    val viewModel: AppViewModel = getViewModel()
    val conference by viewModel.conference.collectAsStateWithLifecycle()

    if (conference == null) {
        // Reading from the settings
        CircularProgressIndicator()
    } else {
        val initialRoute = if (conference == CONFERENCE_NOT_SET) {
            ConferencesDestination.route
        } else {
            SessionsDestination.route
        }
        val scope = rememberCoroutineScope()

        NavHost(
            navController = navController,
            startDestination = initialRoute,
        ) {
            conferencesGraph(
                navigateToConference = {
                    scope.launch {
                        viewModel.setConference(it)
                    }
                }
            )
            sessionsGraph(
                appState = appState,
                displayFeatures = displayFeatures,
                conference = conference!!,
                navigateToSession = {
                    appState.navigate(
                        SessionDetailsDestination,
                        SessionDetailsDestination.createNavigationRoute(conference = conference!!, it)
                    )
                },
                navigateToSignIn = {
                    appState.navigate(
                        SignInDestination,
                        SignInDestination.route
                    )
                },
                onSignOut = {
                    appState.navigate(
                        ConferencesDestination, null
                    )
                },
                onSwitchConferenceSelected = {
                    appState.navigate(
                        ConferencesDestination, null
                    )
                }
            )
            sessionDetailsGraph(appState::onBackClick)
            speakersGraph(
                conference = conference!!,
                appState = appState,
                navigateToSpeaker = {
                    appState.navigate(
                        SpeakerDetailsDestination,
                        SpeakerDetailsDestination.createNavigationRoute(conference = conference!!, it)
                    )
                },
                onSignIn = {
                    appState.navigate(
                        SignInDestination,
                        SignInDestination.route
                    )
                },
                onSignOut = {
                    appState.navigate(
                        ConferencesDestination, null
                    )
                },
                onSwitchConference = {
                    appState.navigate(
                        ConferencesDestination, null
                    )
                }
            )
            speakerDetailsGraph(appState::onBackClick)
            signInGraph(appState::onBackClick)
        }
    }

    val analyticsLogger: AnalyticsLogger = get()
    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect { navEntry ->
            analyticsLogger.logNavigationEvent(conference ?: "", navEntry)
        }
    }
}
