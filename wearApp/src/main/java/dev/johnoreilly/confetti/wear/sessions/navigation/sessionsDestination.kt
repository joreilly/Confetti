@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.sessions.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.sessions.SessionsRoute
import kotlinx.datetime.LocalDate

object SessionsDestination : ConfettiNavigationDestination {
    const val dateArg = "date"
    override val route = "sessions_route/{${dateArg}}"
    override val destination = "sessions_destination"

    fun createNavigationRoute(date: LocalDate): String {
        return "sessions_route/$date"
    }

    fun fromNavArgs(entry: NavBackStackEntry): LocalDate {
        val dateString = entry.arguments?.getString(dateArg)!!
        return LocalDate.parse(dateString)
    }
}

fun NavGraphBuilder.sessionsGraph(
    navigateToSession: (String) -> Unit,
) {
    scrollable(
        route = SessionsDestination.route,
        arguments = listOf(
            navArgument(SessionsDestination.dateArg) { type = NavType.StringType }
        ),
    ) {
        val date = SessionsDestination.fromNavArgs(it.backStackEntry)

        SessionsRoute(date = date, navigateToSession = navigateToSession)
    }
}
