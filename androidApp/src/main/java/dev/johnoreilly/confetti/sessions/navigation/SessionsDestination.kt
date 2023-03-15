package dev.johnoreilly.confetti.sessions.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.window.layout.DisplayFeature
import dev.johnoreilly.confetti.AppSettings.Companion.CONFERENCE_NOT_SET
import dev.johnoreilly.confetti.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.sessions.SessionsRoute

object SessionsDestination : ConfettiNavigationDestination {
    const val conferenceArg = "conference"
    override val route = "sessions_route/{$conferenceArg}"
    override val destination = "sessions_destination"

    fun createNavigationRoute(conference: String): String {
        return "sessions_route/$conference"
    }
}

fun NavGraphBuilder.sessionsGraph(
    isExpandedScreen: Boolean,
    displayFeatures: List<DisplayFeature>,
    navigateToSession: (String) -> Unit,
    navigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConferenceSelected: () -> Unit,
    conference: String,
) {
    composable(
        route = SessionsDestination.route,
        arguments = listOf(
            navArgument(SessionsDestination.conferenceArg) {
                type = NavType.StringType
                defaultValue = "cds"
            }
        )
    ) { backStackEntry ->
        SessionsRoute(
            isExpandedScreen = isExpandedScreen,
            displayFeatures = displayFeatures,
            navigateToSession = navigateToSession,
            onSignOut = onSignOut,
            navigateToSignIn = navigateToSignIn,
            onSwitchConferenceSelected = onSwitchConferenceSelected,
            conference = backStackEntry.arguments?.getString(SessionsDestination.conferenceArg) ?: CONFERENCE_NOT_SET
        )
    }
}
