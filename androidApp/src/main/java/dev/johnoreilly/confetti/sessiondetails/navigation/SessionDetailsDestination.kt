package dev.johnoreilly.confetti.sessiondetails.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.johnoreilly.confetti.navigation.urlDecoded
import dev.johnoreilly.confetti.navigation.urlEncoded
import dev.johnoreilly.confetti.sessiondetails.SessionDetailsRoute

private const val base = "session_details"
private const val sessionIdArg = "sessionIdArg"
private const val conferenceArg = "conferenceArg"

private val arguments = listOf(
    navArgument(conferenceArg) { type = NavType.StringType },
    navArgument(sessionIdArg) { type = NavType.StringType },
)

private val pattern = "$base/{$conferenceArg}/{$sessionIdArg}"

class SessionDetailsKey(val conference: String, val sessionId: String) {
    constructor(navBackStackEntry: NavBackStackEntry): this(
        navBackStackEntry.arguments!!.getString(conferenceArg)!!.urlDecoded(),
        navBackStackEntry.arguments!!.getString(sessionIdArg)!!.urlDecoded(),
    )

    val route: String = "$base/${conference.urlEncoded()}/${sessionId.urlEncoded()}"
}

fun NavGraphBuilder.sessionDetailsGraph(onBackClick: () -> Unit) {
    composable(
        route = pattern,
        arguments = arguments
    ) {
        val key = SessionDetailsKey(it)
        SessionDetailsRoute(key.conference, key.sessionId, onBackClick)
    }
}
