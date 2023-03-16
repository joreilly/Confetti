package dev.johnoreilly.confetti.sessiondetails.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.johnoreilly.confetti.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.sessiondetails.SessionDetailsRoute


object SessionDetailsDestination : ConfettiNavigationDestination {
    const val sessionIdArg = "sessionId"
    const val conferenceArg = "conferenceArg"
    override val route = "session_details_route/{$conferenceArg}/{$sessionIdArg}"
    override val destination = "person_details_destination"

    fun createNavigationRoute(conference: String, sessionId: String): String {
        val encodedId = Uri.encode(sessionId)
        return "session_details_route/$conference/$encodedId"
    }

    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(sessionIdArg)!!
        return Uri.decode(encodedId)
    }
}


fun NavGraphBuilder.sessionDetailsGraph(onBackClick: () -> Unit) {
    composable(
        route = SessionDetailsDestination.route,
        arguments = listOf(
            navArgument(SessionDetailsDestination.sessionIdArg) { type = NavType.StringType },
            navArgument(SessionDetailsDestination.conferenceArg) { type = NavType.StringType }
        )
    ) {
        SessionDetailsRoute(onBackClick)
    }
}
