package dev.johnoreilly.confetti.conferences.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.johnoreilly.confetti.conferences.ConferencesRoute
import dev.johnoreilly.confetti.sessions.navigation.SessionsKey

private const val base = "conferences"

private val pattern = base

object ConferencesKey {
    val route = base
}

fun NavGraphBuilder.conferencesGraph(navigateToConference: (SessionsKey) -> Unit) {
    composable(route = pattern) {
        ConferencesRoute(navigateToConference)
    }
}
