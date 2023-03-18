package dev.johnoreilly.confetti.speakers.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.johnoreilly.confetti.navigation.urlDecoded
import dev.johnoreilly.confetti.navigation.urlEncoded
import dev.johnoreilly.confetti.speakerdetails.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.speakers.SpeakersRoute
import dev.johnoreilly.confetti.ui.ConfettiAppState

private const val base = "speakers"
private const val conferenceArg = "conference"

val speakersRoutePattern = "$base/{$conferenceArg}"

internal class SpeakersKey(val conference: String) {
    constructor(backStackEntry: NavBackStackEntry) :
        this(
            backStackEntry.arguments!!.getString(conferenceArg)!!.urlDecoded(),
        )

    val route: String = "$base/${conference.urlEncoded()}"
}

fun NavGraphBuilder.speakersGraph(
    appState: ConfettiAppState,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConference: () -> Unit
) {
    composable(
        route = speakersRoutePattern,
        arguments = listOf(
            navArgument(conferenceArg) {
                type = NavType.StringType
            },
        )
    ) {
        SpeakersRoute(
            conference = SpeakersKey(it).conference,
            appState = appState,
            navigateToSpeaker = navigateToSpeaker,
            onSignIn = onSignIn,
            onSignOut = onSignOut,
            onSwitchConference = onSwitchConference,
        )
    }
}
