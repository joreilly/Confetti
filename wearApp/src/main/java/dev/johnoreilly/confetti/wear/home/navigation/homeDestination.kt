@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.home.navigation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.wear.home.HomeRoute
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.sessions.navigation.ConferenceDateKey
import kotlinx.datetime.LocalDate

object HomeDestination : ConfettiNavigationDestination {
    const val conferenceArg = "conference"
    override val route = "home_route/{$conferenceArg}"
    override val destination = "home_destination"

    fun createNavigationRoute(conference: String): String {
        val encodedConference = Uri.encode(conference)
        return "home_route/$encodedConference"
    }

    fun fromNavArgs(entry: NavBackStackEntry): String {
        val arguments = entry.arguments!!
        return Uri.decode(arguments.getString(SessionDetailsDestination.conferenceArg))
    }

    fun fromNavArgs(savedStateHandle: SavedStateHandle): String {
        return Uri.decode(savedStateHandle[SessionDetailsDestination.conferenceArg]!!)
    }
}

fun NavGraphBuilder.homeGraph(
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDateKey) -> Unit,
    navigateToSettings: () -> Unit,
) {
    scrollable(
        route = HomeDestination.route,
        arguments = listOf(
            navArgument(HomeDestination.conferenceArg) { type = NavType.StringType },
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern =
                    "confetti://confetti/home/{${HomeDestination.conferenceArg}}}"
            }
        )
    ) {
        HomeRoute(
            navigateToSession = navigateToSession,
            navigateToDay = navigateToDay,
            navigateToSettings = navigateToSettings,
            columnState = it.columnState
        )
    }
}
