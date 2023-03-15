package dev.johnoreilly.confetti.wear.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.google.android.horologist.compose.navscaffold.WearNavScaffold
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
import dev.johnoreilly.confetti.wear.auth.navigation.SignInDestination
import dev.johnoreilly.confetti.wear.auth.navigation.SignOutDestination
import dev.johnoreilly.confetti.wear.auth.navigation.authGraph

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

    WearNavScaffold(
        startDestination = ConferenceHomeDestination.route,
        navController = navController
    ) {
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
            }
        )

        sessionsGraph(
            navigateToSession = {
                onNavigateToDestination(
                    SessionDetailsDestination,
                    SessionDetailsDestination.createNavigationRoute(it)
                )
            },
        )

        sessionDetailsGraph()

        settingsGraph(
            onSwitchConferenceSelected = {
                onNavigateToDestination(ConferencesDestination)
            },
            navigateToGoogleSignOut = { onNavigateToDestination(SignOutDestination) },
            navigateToGoogleSignIn = {  onNavigateToDestination(SignInDestination) }
        )

        authGraph(
            navigateUp = { navController.popBackStack() },
            navigateToGoogleSignIn = { onNavigateToDestination(SignInDestination) },
        )
    }
}