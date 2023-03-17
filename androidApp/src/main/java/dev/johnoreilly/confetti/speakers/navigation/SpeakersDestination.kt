package dev.johnoreilly.confetti.speakers.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.johnoreilly.confetti.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.speakers.SpeakersRoute
import dev.johnoreilly.confetti.ui.ConfettiAppState

object SpeakersDestination : ConfettiNavigationDestination {
    override val route = "speakers_route"
    override val destination = "speakers_destination"
}

fun NavGraphBuilder.speakersGraph(
    conference: String,
    appState: ConfettiAppState,
    navigateToSpeaker: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConference: () -> Unit
) {
    composable(route = SpeakersDestination.route) {
        SpeakersRoute(
            conference = conference,
            appState = appState,
            navigateToSpeaker = navigateToSpeaker,
            onSignIn = onSignIn,
            onSignOut = onSignOut,
            onSwitchConference = onSwitchConference,
        )
    }
}
