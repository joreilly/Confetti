package dev.johnoreilly.confetti.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.window.layout.DisplayFeature
import dev.johnoreilly.confetti.conferences.navigation.ConferencesDestination
import dev.johnoreilly.confetti.conferences.navigation.conferencesGraph
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.sessiondetails.navigation.sessionDetailsGraph
import dev.johnoreilly.confetti.rooms.navigation.SessionsDestination
import dev.johnoreilly.confetti.rooms.navigation.sessionsGraph
import dev.johnoreilly.confetti.sessions.navigation.roomsGraph
import dev.johnoreilly.confetti.spakerdetails.navigation.SpeakerDetailsDestination
import dev.johnoreilly.confetti.spakerdetails.navigation.speakerDetailsGraph
import dev.johnoreilly.confetti.speakers.navigation.speakersGraph

@Composable
fun ConfettiNavHost(
    navController: NavHostController,
    isExpandedScreen: Boolean,
    displayFeatures: List<DisplayFeature>,
    onNavigateToDestination: (ConfettiNavigationDestination, String?) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    startDestination: String = SessionsDestination.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {

        conferencesGraph { conference ->
            onNavigateToDestination(
                SessionsDestination, null
            )
        }

        sessionsGraph(isExpandedScreen, displayFeatures,
            navigateToSession = {
                onNavigateToDestination(
                    SessionDetailsDestination,
                    SessionDetailsDestination.createNavigationRoute(it)
                )
            },
            onSwitchConferenceSelected = {
                onNavigateToDestination(
                    ConferencesDestination, null
                )
            }
        )
        sessionDetailsGraph(onBackClick)

        speakersGraph(isExpandedScreen,
            navigateToSpeaker = {
                onNavigateToDestination(
                    SpeakerDetailsDestination,
                    SpeakerDetailsDestination.createNavigationRoute(it)
                )
            }
        )
        speakerDetailsGraph(onBackClick)
        roomsGraph()
    }
}
