package dev.johnoreilly.confetti.speakers.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.johnoreilly.confetti.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.speakers.SpeakersRoute

object SpeakersDestination : ConfettiNavigationDestination {
    override val route = "speakers_route"
    override val destination = "speakers_destination"
}

fun NavGraphBuilder.speakersGraph(isExpandedScreen: Boolean, navigateToSpeaker: (String) -> Unit) {
    composable(route = SpeakersDestination.route) {
        SpeakersRoute(isExpandedScreen, navigateToSpeaker)
    }
}
