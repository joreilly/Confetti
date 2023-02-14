@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.sessions.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.sessions.SessionsRoute
import kotlinx.datetime.LocalDate

object SessionsDestination : ConfettiNavigationDestination {
    const val dateArg = "date"
    const val conferenceArg = "conference"
    override val route = "sessions_route/{$conferenceArg}/{${dateArg}}"
    override val destination = "sessions_destination"

    fun createNavigationRoute(date: ConferenceDateKey): String {
        return "sessions_route/${date.conference}/${date.date}"
    }

    fun fromNavArgs(entry: NavBackStackEntry): ConferenceDateKey {
        val arguments = entry.arguments!!
        val dateString = entry.arguments?.getString(dateArg)!!
        return ConferenceDateKey(arguments.getString(SessionDetailsDestination.conferenceArg)!!, LocalDate.parse(dateString))
    }
}

data class ConferenceDateKey(val conference: String, val date: LocalDate)

fun NavGraphBuilder.sessionsGraph(
    navigateToSession: (SessionDetailsKey) -> Unit,
) {
    scrollable(
        route = SessionsDestination.route,
        arguments = listOf(
            navArgument(SessionsDestination.dateArg) { type = NavType.StringType }
        ),
    ) {
        val date = SessionsDestination.fromNavArgs(it.backStackEntry)

        SessionsRoute(
            date = date,
            navigateToSession = navigateToSession,
            columnState = it.columnState
        )
    }
}
