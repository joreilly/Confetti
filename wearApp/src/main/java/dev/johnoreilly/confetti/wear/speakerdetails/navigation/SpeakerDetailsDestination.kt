@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.speakerdetails.navigation

import android.net.Uri
import androidx.navigation.*
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.speakerdetails.SpeakerDetailsRoute


object SpeakerDetailsDestination : ConfettiNavigationDestination {
    const val speakerIdArg = "speakerId"
    const val conferenceArg = "conferenceArg"
    override val route = "speaker_details_route/{$conferenceArg}/{$speakerIdArg}"
    override val destination = "speaker_details_destination"

    fun createNavigationRoute(speakerKey: SpeakerDetailsKey): String {
        val encodedId = Uri.encode(speakerKey.speakerId)
        return "speaker_details_route/${speakerKey.conference}/$encodedId"
    }

    fun fromNavArgs(entry: NavBackStackEntry): SpeakerDetailsKey {
        val conference = entry.arguments?.getString(conferenceArg)!!
        val encodedId = entry.arguments?.getString(speakerIdArg)!!
        return SpeakerDetailsKey(conference, Uri.decode(encodedId))
    }
}


fun NavGraphBuilder.speakerDetailsGraph() {
    scrollable(
        route = SpeakerDetailsDestination.route,
        arguments = listOf(
            navArgument(SpeakerDetailsDestination.speakerIdArg) { type = NavType.StringType },
            navArgument(SpeakerDetailsDestination.conferenceArg) { type = NavType.StringType },
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern =
                    "confetti://confetti/speaker/{${SpeakerDetailsDestination.conferenceArg}}/{${SpeakerDetailsDestination.speakerIdArg}}"
            }
        )
    ) {
        SpeakerDetailsRoute(it.columnState)
    }
}