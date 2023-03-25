package dev.johnoreilly.confetti.search.navigation

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

private const val base = "search"
private const val conferenceArg = "conference"

private val routePattern = "$base/{$conferenceArg}"

class SearchKey(val conference: String) {

    constructor(entry: NavBackStackEntry) :
        this(entry.arguments!!.getString(conferenceArg)!!.urlDecoded())

    val route: String = "$base/${conference.urlEncoded()}"
}

fun NavGraphBuilder.searchGraph(
    appState: ConfettiAppState,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConferenceSelected: () -> Unit,
) {
    composable(
        route = routePattern,
        arguments = listOf(
            navArgument(conferenceArg) {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        SessionsView(
            conference = SearchKey(backStackEntry).conference,
            appState = appState,
            navigateToSession = navigateToSession,
            onSignOut = onSignOut,
            navigateToSignIn = navigateToSignIn,
            onSwitchConferenceSelected = onSwitchConferenceSelected,
        )
    }
}
