@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.conferences.navigation

import androidx.navigation.NavGraphBuilder
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.wear.conferences.ConferencesRoute
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination

object ConferencesDestination : ConfettiNavigationDestination {
    override val route = "conferences_route"
    override val destination = "conferences_destination"
}

fun NavGraphBuilder.conferencesGraph(navigateToConference: (String) -> Unit) {
    scrollable(route = ConferencesDestination.route) {
        ConferencesRoute(navigateToConference, it.columnState)
    }
}
