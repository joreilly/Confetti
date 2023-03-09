@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.home.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.home.HomeRoute
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.sessions.navigation.SessionsDestination

object ConferenceHomeDestination : ConfettiNavigationDestination {
    const val conferenceArg = "conference"
    override val route = "conference_route/{${conferenceArg}}"
    override val destination = "conference_destination"

    fun createNavigationRoute(conference: String): String {
        return "conference_route/${conference}"
    }

    fun fromNavArgs(entry: NavBackStackEntry): String {
        val arguments = entry.arguments!!
        return arguments.getString(SessionDetailsDestination.conferenceArg)!!
    }

    fun fromNavArgs(savedStateHandle: SavedStateHandle): String {
        return savedStateHandle[SessionsDestination.conferenceArg]!!
    }
}

fun NavGraphBuilder.conferenceHomeGraph(
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDayKey) -> Unit,
    navigateToSettings: () -> Unit,
) {
    scrollable(
        route = ConferenceHomeDestination.route,
        arguments = listOf(
            navArgument(ConferenceHomeDestination.conferenceArg) {
                type = NavType.StringType
            }
        ),
    ) {
        val conference = ConferenceHomeDestination.fromNavArgs(it.backStackEntry)

        HomeRoute(
            conference = conference,
            navigateToSession = navigateToSession,
            navigateToDay = navigateToDay,
            navigateToSettings = navigateToSettings,
            columnState = it.columnState
        )
    }
}
