package dev.johnoreilly.confetti.splash.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.johnoreilly.confetti.conferences.navigation.ConferencesKey
import dev.johnoreilly.confetti.sessions.navigation.SessionsKey
import dev.johnoreilly.confetti.splash.SplashRoute

private const val base = "splash"

private val pattern = base

object SplashKey {
    val route = base
}

fun NavGraphBuilder.splashGraph(
    navigateToConferences: (ConferencesKey) -> Unit,
    navigateToSessions: (SessionsKey) -> Unit
) {
    composable(
        route = pattern,
    ) {
        SplashRoute(
            navigateToConferences = navigateToConferences,
            navigateToSessions = navigateToSessions
        )
    }
}
