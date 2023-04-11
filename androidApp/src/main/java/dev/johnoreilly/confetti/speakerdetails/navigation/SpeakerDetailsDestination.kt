package dev.johnoreilly.confetti.speakerdetails.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import dev.johnoreilly.confetti.navigation.urlDecoded
import dev.johnoreilly.confetti.navigation.urlEncoded
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.speakerdetails.SpeakerDetailsRoute


private const val base = "speakers"
private const val conferenceArg = "conference"
private const val speakerIdArg = "speakerId"

private val arguments = listOf(
    navArgument(conferenceArg) { type = NavType.StringType },
    navArgument(speakerIdArg) { type = NavType.StringType },
)

private val pattern = "$base/{$conferenceArg}/{$speakerIdArg}"

class SpeakerDetailsKey(val conference: String, val speakerId: String) {
    constructor(backStackEntry: NavBackStackEntry) :
        this(
            backStackEntry.arguments!!.getString(conferenceArg)!!.urlDecoded(),
                backStackEntry.arguments!!.getString(speakerIdArg)!!.urlDecoded(),
        )

    val route: String = "$base/${conference.urlEncoded()}/${speakerId.urlEncoded()}"
}


fun NavGraphBuilder.speakerDetailsGraph(
    navigateToSession: (SessionDetailsKey) -> Unit,
    onBackClick: () -> Unit
) {
    composable(
        route = pattern,
        arguments = arguments,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "confetti://confetti/speakers/{${conferenceArg}}/{${speakerIdArg}}"
            }
        )
    ) { backStackEntry ->
        SpeakerDetailsRoute(
            conference = SpeakerDetailsKey(backStackEntry).conference,
            SpeakerDetailsKey(backStackEntry),
            navigateToSession,
            onBackClick
        )
    }
}
