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
import dev.johnoreilly.confetti.bookmarks.navigation.bookmarksGraph
import dev.johnoreilly.confetti.conferences.navigation.ConferencesKey
import dev.johnoreilly.confetti.conferences.navigation.conferencesGraph
import dev.johnoreilly.confetti.initial_loading.navigation.InitialLoadingKey
import dev.johnoreilly.confetti.initial_loading.navigation.initialLoadingGraph
import dev.johnoreilly.confetti.search.navigation.searchGraph
import dev.johnoreilly.confetti.sessiondetails.navigation.sessionDetailsGraph
import dev.johnoreilly.confetti.sessions.navigation.sessionsGraph
import dev.johnoreilly.confetti.settings.navigation.settingsGraph
import dev.johnoreilly.confetti.speakerdetails.navigation.speakerDetailsGraph
import dev.johnoreilly.confetti.speakers.navigation.speakersGraph
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
        startDestination = InitialLoadingKey.route,
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

        initialLoadingGraph(
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
        sessionDetailsGraph(
            appState::onBackClick,
            navigateToSpeakerDetails = { key ->
                appState.navigate(key.route)
            }
        )
        speakersGraph(
            appState = appState,
            navigateToSpeaker = { appState.navigate(it.route) },
            onSignIn = ::onSignIn,
            onSignOut = ::onSignOut,
            onSwitchConference = ::onSwitchConference,
        )
        speakerDetailsGraph(
            navigateToSession = {
                appState.navigate(it.route)
            },
            appState::onBackClick
        )
        signInGraph(appState::onBackClick)
        searchGraph(
            appState = appState,
            navigateToSession = { appState.navigate(it.route) },
            navigateToSpeaker = { appState.navigate(it.route) },
            navigateToSignIn = ::onSignIn,
            onSignOut = ::onSignOut,
            onSwitchConferenceSelected = ::onSwitchConference
        )
        bookmarksGraph(
            appState = appState,
            navigateToSession = { appState.navigate(it.route) },
            navigateToSignIn = ::onSignIn,
            onSignOut = ::onSignOut,
            onSwitchConferenceSelected = ::onSwitchConference
        )
        settingsGraph()
    }

    val analyticsLogger: AnalyticsLogger = get()
    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect { navEntry ->
            analyticsLogger.logNavigationEvent(navEntry)
        }
    }
}
