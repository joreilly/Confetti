@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.conferences.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import androidx.wear.compose.navigation.composable
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import dev.johnoreilly.confetti.wear.conferences.ConferencesRoute
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination

object ConferencesDestination : ConfettiNavigationDestination {
    override val route = "conferences_route"
    override val destination = "conferences_destination"
}

fun NavGraphBuilder.conferencesGraph(navigateToConference: (String) -> Unit) {
    composable(
        route = ConferencesDestination.route,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "confetti://confetti/conferences"
            }
        )
    ) {
        ConferencesRoute(navigateToConference, ScalingLazyColumnDefaults.belowTimeText().create())
    }
}
