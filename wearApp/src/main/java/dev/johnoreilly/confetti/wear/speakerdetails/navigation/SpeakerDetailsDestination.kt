@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.speakerdetails.navigation

import android.net.Uri
import androidx.navigation.*
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.wear.speakerdetails.SpeakerDetailsRoute
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination


object SpeakerDetailsDestination : ConfettiNavigationDestination {
    const val speakerIdArg = "speakerId"
    const val conferenceArg = "conferenceArg"
    override val route = "speaker_details_route/{$conferenceArg}/{$speakerIdArg}"
    override val destination = "speaker_details_destination"

    fun createNavigationRoute(spakerKey: SpeakerDetailsKey): String {
        val encodedId = Uri.encode(spakerKey.speakerId)
        return "speaker_details_route/${spakerKey.conference}/$encodedId"
    }

    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(speakerIdArg)!!
        return Uri.decode(encodedId)
    }
}


fun NavGraphBuilder.speakerDetailsGraph() {
    scrollable(
        route = SpeakerDetailsDestination.route,
        arguments = listOf(
            navArgument(SpeakerDetailsDestination.speakerIdArg) { type = NavType.StringType },
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern =
                    "confetti://confetti/speaker/{${SpeakerDetailsDestination.speakerIdArg}}"
            }
        )
    ) {
        SpeakerDetailsRoute(it.columnState)
    }
}