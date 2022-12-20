package dev.johnoreilly.confetti.wear.ui


import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.horologist.compose.navscaffold.WearNavScaffold
import dev.johnoreilly.confetti.wear.conferences.navigation.ConferencesDestination
import dev.johnoreilly.confetti.wear.conferences.navigation.conferencesGraph
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.rooms.navigation.roomsGraph
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.sessionDetailsGraph
import dev.johnoreilly.confetti.wear.sessions.navigation.SessionsDestination
import dev.johnoreilly.confetti.wear.sessions.navigation.sessionsGraph

@Composable
fun ConfettiApp() {
    val navController = rememberSwipeDismissableNavController()

    fun onNavigateToDestination(destination: ConfettiNavigationDestination, route: String? = null) {
        navController.navigate(route ?: destination.route)
    }

    fun onBackClick() {
        navController.popBackStack()
    }

    WearNavScaffold(startDestination = SessionsDestination.route, navController = navController) {
        conferencesGraph(navigateToConference = { conference ->
            onNavigateToDestination(SessionsDestination)
        })

        sessionsGraph(
            navigateToSession = {
                onNavigateToDestination(
                    SessionDetailsDestination,
                    SessionDetailsDestination.createNavigationRoute(it)
                )
            },
            onSwitchConferenceSelected = {
                onNavigateToDestination(
                    ConferencesDestination
                )
            }
        )
        sessionDetailsGraph(::onBackClick)

        roomsGraph()
    }
}