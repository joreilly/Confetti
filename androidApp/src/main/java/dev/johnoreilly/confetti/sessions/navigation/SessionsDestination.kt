package dev.johnoreilly.confetti.sessions.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.johnoreilly.confetti.navigation.urlDecoded
import dev.johnoreilly.confetti.navigation.urlEncoded
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.sessions.SessionsView
import dev.johnoreilly.confetti.ui.ConfettiAppState

private const val base = "sessions"
private const val conferenceArg = "conference"

val sessionsRoutePattern = "$base/{$conferenceArg}"

class SessionsKey(val conference: String) {
    constructor(backStackEntry: NavBackStackEntry) :
        this(backStackEntry.arguments!!.getString(conferenceArg)!!.urlDecoded())

    val route: String = "${base}/${conference.urlEncoded()}"
}


fun NavGraphBuilder.sessionsGraph(
    appState: ConfettiAppState,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConferenceSelected: () -> Unit,
) {
    composable(
        route = sessionsRoutePattern,
        arguments = listOf(
            navArgument(conferenceArg) {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        SessionsView(
            conference = SessionsKey(backStackEntry).conference,
            appState = appState,
            navigateToSession = navigateToSession,
            onSignOut = onSignOut,
            navigateToSignIn = navigateToSignIn,
            onSwitchConferenceSelected = onSwitchConferenceSelected,
        )
    }
}
