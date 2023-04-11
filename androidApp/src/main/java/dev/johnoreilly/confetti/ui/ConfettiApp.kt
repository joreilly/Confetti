package dev.johnoreilly.confetti.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import androidx.window.layout.DisplayFeature
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.account.navigation.SigninKey
import dev.johnoreilly.confetti.account.navigation.signInGraph
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.bookmarks.navigation.bookmarksGraph
import dev.johnoreilly.confetti.conferences.navigation.ConferencesKey
import dev.johnoreilly.confetti.conferences.navigation.conferencesGraph
import dev.johnoreilly.confetti.navigation.TopLevelDestination
import dev.johnoreilly.confetti.search.navigation.searchGraph
import dev.johnoreilly.confetti.sessiondetails.navigation.sessionDetailsGraph
import dev.johnoreilly.confetti.sessions.navigation.sessionsGraph
import dev.johnoreilly.confetti.sessions.navigation.sessionsRoutePattern
import dev.johnoreilly.confetti.settings.navigation.settingsGraph
import dev.johnoreilly.confetti.speakerdetails.navigation.speakerDetailsGraph
import dev.johnoreilly.confetti.speakers.navigation.speakersGraph
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ConfettiApp(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    confettiRepository: ConfettiRepository,
    defaultConferenceParameter: String?,
) {
    val appState: ConfettiAppState = rememberConfettiAppState(
        windowSizeClass,
        displayFeatures,
        navController
    )

    val coroutineScope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = sessionsRoutePattern,
        route = "Root",
    ) {
        fun onSignIn() {
            appState.navigate(SigninKey.route)
        }

        fun onSignOut() {
            appState.navigate(ConferencesKey.route)
        }

        fun onSwitchConference() {
            coroutineScope.launch {
                confettiRepository.setConference(AppSettings.CONFERENCE_NOT_SET)
                TopLevelDestination.values.forEach { destination ->
                    // We *must* do this, else, the previously saved destinations will take
                    // precedence over the new routes we pass in. This means that they will be
                    // referencing the old conference argument, despite us sending in a new one,
                    // which seems to be completely ignored.
                    navController.clearBackStack(destination.routePattern)
                }
                navController.navigate(
                    route = ConferencesKey.route,
                    navOptions = navOptions {
                        popUpTo(navController.graph.findStartDestination().id) {
                            // Pop the entire backstack when going to the switch conference screen
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                )
            }
        }

        sessionsGraph(
            appState = appState,
            navigateToSession = { appState.navigate(it.route) },
            navigateToSignIn = ::onSignIn,
            onSignOut = ::onSignOut,
            onSwitchConferenceSelected = ::onSwitchConference,
            defaultConferenceParameter = defaultConferenceParameter,
        )
        conferencesGraph(
            navigateToConference = { sessionsKey ->
                appState.navController.navigate(
                    route = sessionsKey.route,
                    navOptions = navOptions {
                        popUpTo(ConferencesKey.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                )
            }
        )
        sessionDetailsGraph(
            appState::onBackClick,
            navigateToSignIn = ::onSignIn,
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

    val analyticsLogger: AnalyticsLogger = koinInject()
    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow.collect { navEntry ->
            analyticsLogger.logNavigationEvent(navEntry)
        }
    }
}
