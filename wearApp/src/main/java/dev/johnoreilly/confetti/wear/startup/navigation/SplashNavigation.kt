@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.startup.navigation

import androidx.navigation.NavGraphBuilder
import androidx.wear.compose.navigation.composable
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.startup.InitialLoadingRoute

object StartupDestination : ConfettiNavigationDestination {
    override val route = "startup_route"
    override val destination = "startup_destination"
}

fun NavGraphBuilder.initialLoadingGraph(
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDayKey) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToBookmarks: (String) -> Unit,
    navigateToConferences: () -> Unit
) {
    composable(
        route = StartupDestination.route,
    ) {
        InitialLoadingRoute(
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            navigateToSession = navigateToSession,
            navigateToDay = navigateToDay,
            navigateToSettings = navigateToSettings,
            navigateToBookmarks = navigateToBookmarks,
            navigateToConferences = navigateToConferences
        )
    }
}
