package dev.johnoreilly.confetti.ui


import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.window.layout.DisplayFeature
import dev.johnoreilly.confetti.account.navigation.SigninKey
import dev.johnoreilly.confetti.account.navigation.signInGraph
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.conferences.navigation.ConferencesKey
import dev.johnoreilly.confetti.conferences.navigation.conferencesGraph
import dev.johnoreilly.confetti.sessiondetails.navigation.sessionDetailsGraph
import dev.johnoreilly.confetti.sessions.navigation.sessionsGraph
import dev.johnoreilly.confetti.speakerdetails.navigation.speakerDetailsGraph
import dev.johnoreilly.confetti.speakers.navigation.speakersGraph
import dev.johnoreilly.confetti.splash.navigation.SplashKey
import dev.johnoreilly.confetti.splash.navigation.splashGraph
import org.koin.androidx.compose.get

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

    NavHost(
        navController = navController,
        startDestination = SplashKey.route,
    ) {
        fun onSignIn() {
            appState.navigate(SigninKey.route)
        }

        fun onSignOut() {
            appState.navigate(ConferencesKey.route)
        }

        fun onSwitchConference() {
            appState.navigateToTopLevelDestination(ConferencesKey.route)
        }

        splashGraph(
            navigateToConferences = { appState.navigateToTopLevelDestination(it.route) },
            navigateToSessions = { appState.navigateToTopLevelDestination(it.route) }
        )
        conferencesGraph(
            navigateToConference = { appState.navigateToTopLevelDestination(it.route) }
        )
        sessionsGraph(
            appState = appState,
            navigateToSession = { appState.navigate(it.route) },
            navigateToSignIn = ::onSignIn,
            onSignOut = ::onSignOut,
            onSwitchConferenceSelected = ::onSwitchConference
        )
        sessionDetailsGraph(appState::onBackClick)
        speakersGraph(
            appState = appState,
            navigateToSpeaker = { appState.navigate(it.route) },
            onSignIn = ::onSignIn,
            onSignOut = ::onSignOut,
            onSwitchConference = ::onSwitchConference,
        )
        speakerDetailsGraph(appState::onBackClick)
        signInGraph(appState::onBackClick)
    }

    val analyticsLogger: AnalyticsLogger = get()
    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect { navEntry ->
            analyticsLogger.logNavigationEvent(navEntry)
        }
    }
}
