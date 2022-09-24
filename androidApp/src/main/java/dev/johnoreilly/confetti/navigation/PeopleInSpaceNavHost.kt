package dev.johnoreilly.confetti.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.sessiondetails.navigation.sessionDetailsGraph
import dev.johnoreilly.confetti.rooms.navigation.SessionsDestination
import dev.johnoreilly.confetti.rooms.navigation.sessionsGraph
import dev.johnoreilly.confetti.sessions.navigation.roomsGraph
import dev.johnoreilly.confetti.speakers.navigation.speakersGraph

@Composable
fun ConfettiNavHost(
    navController: NavHostController,
    onNavigateToDestination: (ConfettiNavigationDestination, String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    startDestination: String = SessionsDestination.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        sessionsGraph(navigateToSession = {
            onNavigateToDestination(
                SessionDetailsDestination,
                SessionDetailsDestination.createNavigationRoute(it)
            )
        })
        sessionDetailsGraph(onBackClick)
        speakersGraph()
        roomsGraph()
    }
}
