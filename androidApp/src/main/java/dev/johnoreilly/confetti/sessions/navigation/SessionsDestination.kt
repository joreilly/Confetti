package dev.johnoreilly.confetti.rooms.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.johnoreilly.confetti.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.sessions.SessionsRoute

object SessionsDestination : ConfettiNavigationDestination {
    override val route = "sessions_route"
    override val destination = "sessions_destination"
}

fun NavGraphBuilder.sessionsGraph(navigateToSession: (String) -> Unit) {
    composable(route = SessionsDestination.route) {
        SessionsRoute(navigateToSession)
    }
}
