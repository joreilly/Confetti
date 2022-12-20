@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.sessions.navigation

import androidx.navigation.NavGraphBuilder
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.NavScaffoldViewModel
import com.google.android.horologist.compose.navscaffold.composable
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.sessions.SessionsRoute

object SessionsDestination : ConfettiNavigationDestination {
    override val route = "sessions_route"
    override val destination = "sessions_destination"
}

fun NavGraphBuilder.sessionsGraph(
    navigateToSession: (String) -> Unit,
    onSwitchConferenceSelected: () -> Unit,
) {
    composable(route = SessionsDestination.route) {
        // Defer scaffold to each page of the pager
        it.timeTextMode = NavScaffoldViewModel.TimeTextMode.Off

        SessionsRoute(navigateToSession, onSwitchConferenceSelected)
    }
}
