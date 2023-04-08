@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.home.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import androidx.wear.compose.navigation.composable
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.home.HomeRoute
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination

object ConferenceHomeDestination : ConfettiNavigationDestination {
    const val conferenceArg = "conference"
    override val route = "conference_route/{${conferenceArg}}"
    override val destination = "conference_destination"

    fun createNavigationRoute(conference: String): String {
        return "conference_route/${conference}"
    }

    fun fromNavArgs(entry: NavBackStackEntry): String {
        val arguments = entry.arguments!!
        return arguments.getString(conferenceArg)!!
    }

    fun fromNavArgs(savedStateHandle: SavedStateHandle): String {
        val conference: String = savedStateHandle[conferenceArg]!!
        return conference
    }
}

fun NavGraphBuilder.conferenceHomeGraph(
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDayKey) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToBookmarks: (String) -> Unit
) {
    composable(
        route = ConferenceHomeDestination.route,
        arguments = listOf(
            navArgument(ConferenceHomeDestination.conferenceArg) {
                type = NavType.StringType
                defaultValue = ""
            }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern =
                    "confetti://confetti/conferenceHome/{${ConferenceHomeDestination.conferenceArg}}"
            }
        )
    ) {
        HomeRoute(
            navigateToSession = navigateToSession,
            navigateToDay = navigateToDay,
            navigateToSettings = navigateToSettings,
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            navigateToBookmarks = navigateToBookmarks
        )
    }
}
