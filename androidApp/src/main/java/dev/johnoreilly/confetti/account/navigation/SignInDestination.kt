package dev.johnoreilly.confetti.account.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.johnoreilly.confetti.account.SignInRoute
import dev.johnoreilly.confetti.navigation.ConfettiNavigationDestination

object SignInDestination : ConfettiNavigationDestination {
    override val route = "signin"
    override val destination = "signin_destination"
}


fun NavGraphBuilder.signInGraph(onBackClick: () -> Unit) {
    composable(
        route = SignInDestination.route,
        arguments = emptyList()
    ) {
        SignInRoute(onBackClick)
    }
}
