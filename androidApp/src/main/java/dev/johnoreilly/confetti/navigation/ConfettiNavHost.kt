package dev.johnoreilly.confetti.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.window.layout.DisplayFeature
import dev.johnoreilly.confetti.account.navigation.SignInDestination
import dev.johnoreilly.confetti.account.navigation.signInGraph
import dev.johnoreilly.confetti.conferences.navigation.ConferencesDestination
import dev.johnoreilly.confetti.conferences.navigation.conferencesGraph
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.sessiondetails.navigation.sessionDetailsGraph
import dev.johnoreilly.confetti.sessions.navigation.SessionsDestination
import dev.johnoreilly.confetti.sessions.navigation.sessionsGraph
import dev.johnoreilly.confetti.speakerdetails.navigation.SpeakerDetailsDestination
import dev.johnoreilly.confetti.speakerdetails.navigation.speakerDetailsGraph
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

}
