package dev.johnoreilly.confetti.spakerdetails.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.johnoreilly.confetti.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.speakerdetails.SpeakerDetailsRoute


object SpeakerDetailsDestination : ConfettiNavigationDestination {
    const val speakerIdArg = "speakerId"
    override val route = "speaker_details_route/{$speakerIdArg}"
    override val destination = "speaker_details_destination"

    fun createNavigationRoute(speakerId: String): String {
        val encodedId = Uri.encode(speakerId)
        return "speaker_details_route/$encodedId"
    }

    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(speakerIdArg)!!
        return Uri.decode(encodedId)
    }
}


fun NavGraphBuilder.speakerDetailsGraph(onBackClick: () -> Unit) {
    composable(
        route = SpeakerDetailsDestination.route,
        arguments = listOf(
            navArgument(SpeakerDetailsDestination.speakerIdArg) { type = NavType.StringType }
        )
    ) {
        SpeakerDetailsRoute(onBackClick)
    }
}
