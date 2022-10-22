package dev.johnoreilly.confetti.conferences.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.johnoreilly.confetti.conferences.ConferencesRoute
import dev.johnoreilly.confetti.navigation.ConfettiNavigationDestination

object ConferencesDestination : ConfettiNavigationDestination {
    override val route = "conferences_route"
    override val destination = "conferences_destination"
}

fun NavGraphBuilder.conferencesGraph(navigateToConference: (String) -> Unit) {
    composable(route = ConferencesDestination.route) {
        ConferencesRoute(navigateToConference)
    }
}
