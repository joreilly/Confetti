@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.auth.ui.googlesignin.signin.GoogleSignInScreen
import androidx.wear.compose.navigation.composable
import dev.johnoreilly.confetti.wear.auth.FirebaseSignOutScreen
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import org.koin.androidx.compose.getViewModel

object SignInDestination : ConfettiNavigationDestination {
    override val route = "signin_route"
    override val destination = "signin_destination"
}

object SignOutDestination : ConfettiNavigationDestination {
    override val route = "signout_route"
    override val destination = "signout_destination"
}

fun NavGraphBuilder.authGraph(
    navigateUp: () -> Unit,
    onAuthChanged: () -> Unit,
) {
    composable(
        route = SignInDestination.route,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "confetti://confetti/signIn"
            }
        )
    ) {
        GoogleSignInScreen(
            onAuthCancelled = navigateUp,
            onAuthSucceed = {
                onAuthChanged()
                navigateUp()
            },
            viewModel = getViewModel()
        )
    }

    composable(
        route = SignOutDestination.route,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "confetti://confetti/signOut"
            }
        )
    ) {
        FirebaseSignOutScreen(
            navigateUp = navigateUp,
            onAuthChanged = onAuthChanged
        )
    }
}