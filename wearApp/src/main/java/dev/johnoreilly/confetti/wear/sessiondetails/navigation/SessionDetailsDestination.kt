@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.sessiondetails.navigation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsRoute


object SessionDetailsDestination : ConfettiNavigationDestination {
    const val sessionIdArg = "sessionId"
    const val conferenceArg = "conference"
    override val route = "session_details_route/{$conferenceArg}/{$sessionIdArg}"
    override val destination = "person_details_destination"

    fun createNavigationRoute(sessionKey: SessionDetailsKey): String {
        val encodedId = Uri.encode(sessionKey.sessionId)
        return "session_details_route/${sessionKey.conference}/$encodedId"
    }

    fun fromNavArgs(entry: NavBackStackEntry): SessionDetailsKey {
        val arguments = entry.arguments!!
        return SessionDetailsKey(
            arguments.getString(conferenceArg)!!,
            Uri.decode(arguments.getString(sessionIdArg))
        )
    }

    fun fromNavArgs(savedStateHandle: SavedStateHandle): SessionDetailsKey {
        return SessionDetailsKey(
            savedStateHandle[conferenceArg]!!,
            Uri.decode(savedStateHandle[sessionIdArg]!!)
        )
    }
}

fun NavGraphBuilder.sessionDetailsGraph(navigateToSpeaker: (SpeakerDetailsKey) -> Unit) {
    scrollable(
        route = SessionDetailsDestination.route,
        arguments = listOf(
            navArgument(SessionDetailsDestination.conferenceArg) { type = NavType.StringType },
            navArgument(SessionDetailsDestination.sessionIdArg) { type = NavType.StringType }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern =
                    "confetti://confetti/session/{${SessionDetailsDestination.conferenceArg}}/{${SessionDetailsDestination.sessionIdArg}}"
            }
        )
    ) {
        SessionDetailsRoute(it.columnState, navigateToSpeaker)
    }
}