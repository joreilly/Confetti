package dev.johnoreilly.confetti.sessions.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.window.layout.DisplayFeature
import dev.johnoreilly.confetti.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.sessions.SessionsRoute

object SessionsDestination : ConfettiNavigationDestination {
    override val route = "sessions_route"
    override val destination = "sessions_destination"
}

fun NavGraphBuilder.sessionsGraph(
    isExpandedScreen: Boolean,
    displayFeatures: List<DisplayFeature>,
    navigateToSession: (String) -> Unit,
    navigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConferenceSelected: () -> Unit,
) {
    composable(route = SessionsDestination.route) {
        SessionsRoute(
            isExpandedScreen,
            displayFeatures,
            navigateToSession,
            navigateToSignIn,
            onSignOut,
            onSwitchConferenceSelected
        )
    }
}
