@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.startup.navigation

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.composable
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.startup.StartHomeRoute

object StartHomeDestination : ConfettiNavigationDestination {
    const val conferenceArg = "conference"
    override val route = "start_route/{${conferenceArg}}"
    override val destination = "start_destination"

    fun createNavigationRoute(conference: String?): String {
        return "start_route/${conference.orEmpty()}"
    }

    fun fromNavArgs(entry: NavBackStackEntry): String? {
        val arguments = entry.arguments!!
        return arguments.getString(conferenceArg)
    }

    fun fromNavArgs(savedStateHandle: SavedStateHandle): String? {
        return savedStateHandle[conferenceArg]
    }
}

fun NavGraphBuilder.startHomeGraph(
    intent: Intent? = null,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDayKey) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToBookmarks: (String) -> Unit,
    navigateToConferences: () -> Unit
) {
    composable(
        route = StartHomeDestination.route,
        arguments = listOf(
            navArgument(StartHomeDestination.conferenceArg) {
                type = NavType.StringType
                this.nullable = true
                // TODO take this from intent URI
                defaultValue = null
            },
        )
    ) {
        StartHomeRoute(
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            navigateToSession = navigateToSession,
            navigateToDay = navigateToDay,
            navigateToSettings = navigateToSettings,
            navigateToBookmarks = navigateToBookmarks,
            navigateToConferences = navigateToConferences
        )
    }
}
