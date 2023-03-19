package dev.johnoreilly.confetti.initial_loading.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.johnoreilly.confetti.conferences.navigation.ConferencesKey
import dev.johnoreilly.confetti.sessions.navigation.SessionsKey
import dev.johnoreilly.confetti.initial_loading.InitialLoadingRoute

private const val base = "initial_loading"

private val pattern = base

object InitialLoadingKey {
    val route = base
}

fun NavGraphBuilder.initialLoadingGraph(
    navigateToConferences: (ConferencesKey) -> Unit,
    navigateToSessions: (SessionsKey) -> Unit
) {
    composable(
        route = pattern,
    ) {
        InitialLoadingRoute(
            navigateToConferences = navigateToConferences,
            navigateToSessions = navigateToSessions
        )
    }
}
