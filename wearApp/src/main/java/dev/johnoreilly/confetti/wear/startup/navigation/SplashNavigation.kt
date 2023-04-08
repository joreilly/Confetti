@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.startup.navigation

import androidx.navigation.NavGraphBuilder
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.composable
import dev.johnoreilly.confetti.wear.AppUiState
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.startup.InitialLoadingRoute

object StartupDestination : ConfettiNavigationDestination {
    override val route = "startup_route"
    override val destination = "startup_destination"
}

fun NavGraphBuilder.initialLoadingGraph(
    navigateToConferences: () -> Unit,
    navigateToHome: (String) -> Unit,
    appUiState: AppUiState?
) {
    composable(
        route = StartupDestination.route,
    ) {
        InitialLoadingRoute(
            navigateToConferences = navigateToConferences,
            navigateToHome = navigateToHome,
            appUiState = appUiState
        )
    }
}
