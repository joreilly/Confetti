package dev.johnoreilly.confetti.bookmarks.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.johnoreilly.confetti.bookmarks.BookmarksRoute
import dev.johnoreilly.confetti.navigation.urlDecoded
import dev.johnoreilly.confetti.navigation.urlEncoded
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.ui.ConfettiAppState

private const val base = "bookmarks"
private const val conferenceArg = "conference"

const val bookmarksRoutePattern = "$base/{$conferenceArg}"

class BookmarksKey(val conference: String) {

    constructor(entry: NavBackStackEntry) :
        this(entry.arguments!!.getString(conferenceArg)!!.urlDecoded())

    val route: String = "$base/${conference.urlEncoded()}"
}

fun NavGraphBuilder.bookmarksGraph(
    appState: ConfettiAppState,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConferenceSelected: () -> Unit,
) {
    composable(
        route = bookmarksRoutePattern,
        arguments = listOf(
            navArgument(conferenceArg) {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        BookmarksRoute(
            conference = BookmarksKey(backStackEntry).conference,
            appState = appState,
            navigateToSession = navigateToSession,
            onSwitchConference = onSwitchConferenceSelected,
            onSignIn = navigateToSignIn,
            onSignOut = onSignOut,
        )
    }
}
