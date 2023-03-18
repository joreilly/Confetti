package dev.johnoreilly.confetti.account.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.johnoreilly.confetti.account.SignInRoute

private const val base = "signin"
private val arguments = emptyList<NamedNavArgument>()

private val pattern = base

object SigninKey {
    val route = base
}

fun NavGraphBuilder.signInGraph(onBackClick: () -> Unit) {
    composable(
        route = pattern,
        arguments = arguments
    ) {
        SignInRoute(onBackClick)
    }
}
