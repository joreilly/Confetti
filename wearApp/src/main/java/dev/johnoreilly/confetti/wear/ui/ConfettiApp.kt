package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.android.horologist.compose.navscaffold.WearNavScaffold
import dev.johnoreilly.confetti.wear.WearAppViewModel
import dev.johnoreilly.confetti.wear.auth.navigation.SignInDestination
import dev.johnoreilly.confetti.wear.auth.navigation.SignOutDestination
import dev.johnoreilly.confetti.wear.auth.navigation.authGraph
import dev.johnoreilly.confetti.wear.bookmarks.navigation.BookmarksDestination
import dev.johnoreilly.confetti.wear.bookmarks.navigation.bookmarksGraph
import dev.johnoreilly.confetti.wear.conferences.navigation.ConferencesDestination
import dev.johnoreilly.confetti.wear.conferences.navigation.conferencesGraph
import dev.johnoreilly.confetti.wear.home.navigation.ConferenceHomeDestination
import dev.johnoreilly.confetti.wear.home.navigation.conferenceHomeGraph
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.sessionDetailsGraph
import dev.johnoreilly.confetti.wear.sessions.navigation.SessionsDestination
import dev.johnoreilly.confetti.wear.sessions.navigation.sessionsGraph
import dev.johnoreilly.confetti.wear.settings.navigation.SettingsDestination
import dev.johnoreilly.confetti.wear.settings.navigation.settingsGraph
import dev.johnoreilly.confetti.wear.speakerdetails.navigation.SpeakerDetailsDestination
import dev.johnoreilly.confetti.wear.speakerdetails.navigation.speakerDetailsGraph
import dev.johnoreilly.confetti.wear.startup.navigation.StartupDestination
import dev.johnoreilly.confetti.wear.startup.navigation.initialLoadingGraph
import org.koin.androidx.compose.getViewModel

@Composable
fun ConfettiApp(
    navController: NavHostController
) {
    fun onNavigateToDestination(destination: ConfettiNavigationDestination, route: String? = null) {
        if (destination is ConferenceHomeDestination) {
            navController.navigate(route ?: destination.route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        } else {
            navController.navigate(route ?: destination.route)
        }
    }

    val viewModel: WearAppViewModel = getViewModel()

    val appState by viewModel.appState.collectAsStateWithLifecycle()

    ConfettiTheme(appState.settings.theme) {
        WearNavScaffold(
            startDestination = StartupDestination.route,
            navController = navController
        ) {
            initialLoadingGraph(
                navigateToConferences = {
                    onNavigateToDestination(
                        ConferencesDestination,
                        ConferencesDestination.route
                    )
                },
                navigateToHome = {
                    onNavigateToDestination(
                        ConferenceHomeDestination,
                        ConferenceHomeDestination.createNavigationRoute(it)
                    )
                },
                appUiState = appState
            )

            conferencesGraph(
                navigateToConference = {
                    onNavigateToDestination(
                        ConferenceHomeDestination,
                        ConferenceHomeDestination.createNavigationRoute(it)
                    )
                }
            )

            conferenceHomeGraph(
                navigateToSession = {
                    onNavigateToDestination(
                        SessionDetailsDestination,
                        SessionDetailsDestination.createNavigationRoute(it)
                    )
                },
                navigateToSettings = {
                    onNavigateToDestination(SettingsDestination)
                },
                navigateToDay = {
                    onNavigateToDestination(
                        SessionsDestination,
                        SessionsDestination.createNavigationRoute(it)
                    )
                },
                navigateToConferenceList = {
                    onNavigateToDestination(
                        ConferencesDestination,
                        ConferencesDestination.route
                    )
                },
                navigateToBookmarks = {
                    onNavigateToDestination(
                        BookmarksDestination,
                        BookmarksDestination.createNavigationRoute(it)
                    )
                }
            )

            sessionsGraph(
                navigateToSession = {
                    onNavigateToDestination(
                        SessionDetailsDestination,
                        SessionDetailsDestination.createNavigationRoute(it)
                    )
                }
            )

            sessionDetailsGraph(
                navigateToSpeaker = {
                    onNavigateToDestination(
                        SpeakerDetailsDestination,
                        SpeakerDetailsDestination.createNavigationRoute(it)
                    )
                }
            )

            speakerDetailsGraph()

            bookmarksGraph(
                navigateToSession = {
                    onNavigateToDestination(
                        SessionDetailsDestination,
                        SessionDetailsDestination.createNavigationRoute(it)
                    )
                }
            )

            settingsGraph(
                onSwitchConferenceSelected = {
                    onNavigateToDestination(ConferencesDestination)
                },
                navigateToGoogleSignOut = { onNavigateToDestination(SignOutDestination) },
                navigateToGoogleSignIn = { onNavigateToDestination(SignInDestination) }
            )

            authGraph(
                navigateUp = { navController.popBackStack() },
                navigateToGoogleSignIn = { onNavigateToDestination(SignInDestination) },
            )
        }
    }
}