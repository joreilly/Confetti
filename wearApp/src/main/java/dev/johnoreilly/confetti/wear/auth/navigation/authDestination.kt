@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.auth.navigation

import androidx.navigation.NavGraphBuilder
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.auth.ui.googlesignin.signin.GoogleSignInScreen
import com.google.android.horologist.compose.navscaffold.composable
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.wear.auth.GoogleSignInPromptScreen
import dev.johnoreilly.confetti.wear.auth.GoogleSignOutScreen
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import org.koin.androidx.compose.getViewModel

object SignInPromptDestination : ConfettiNavigationDestination {
    override val route = "signin_prompt_route"
    override val destination = "signin_prompt_destination"
}

object SignInDestination : ConfettiNavigationDestination {
    override val route = "signin_route"
    override val destination = "signin_destination"
}

object SignOutDestination : ConfettiNavigationDestination {
    override val route = "signout_route"
    override val destination = "signout_destination"
}

fun NavGraphBuilder.authGraph(
    navigateToGoogleSignIn: () -> Unit,
    navigateUp: () -> Unit,
) {

    scrollable(
        route = SignInPromptDestination.route
    ) {
        GoogleSignInPromptScreen(
            navigateToGoogleSignIn = navigateToGoogleSignIn,
            navigateUp = navigateUp,
            columnState = it.columnState,
        )
    }

    composable(route = SignInDestination.route) {
        GoogleSignInScreen(
            onAuthCancelled = navigateUp,
            onAuthSucceed = navigateUp,
            viewModel = getViewModel()
        )
    }

    composable(route = SignOutDestination.route) {
        GoogleSignOutScreen(
            navigateUp = navigateUp,
        )
    }
}