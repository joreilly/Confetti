@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.sessions.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.wear.sessions.SessionsRoute
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate

object SessionsDestination : ConfettiNavigationDestination {
    const val dateArg = "date"
    const val conferenceArg = "conference"
    override val route = "sessions_route/{$conferenceArg}/{${dateArg}}"
    override val destination = "sessions_destination"

    fun createNavigationRoute(date: ConferenceDayKey): String {
        return "sessions_route/${date.conference}/${date.date}"
    }

    fun fromNavArgs(entry: NavBackStackEntry): ConferenceDayKey {
        val arguments = entry.arguments!!
        val dateString = entry.arguments?.getString(dateArg)!!
        return ConferenceDayKey(
            arguments.getString(SessionDetailsDestination.conferenceArg)!!,
            LocalDate.parse(dateString)
        )
    }

    fun fromNavArgs(savedStateHandle: SavedStateHandle): ConferenceDayKey {
        return ConferenceDayKey(
            savedStateHandle[conferenceArg]!!,
            savedStateHandle.get<String>(dateArg)!!.toLocalDate()
        )
    }
}

fun NavGraphBuilder.sessionsGraph(
    navigateToSession: (SessionDetailsKey) -> Unit
) {
    scrollable(
        route = SessionsDestination.route,
        arguments = listOf(
            navArgument(SessionsDestination.dateArg) { type = NavType.StringType },
            navArgument(SessionsDestination.conferenceArg) { type = NavType.StringType }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern =
                    "confetti://confetti/sessions/{${SessionsDestination.conferenceArg}}/{${SessionsDestination.dateArg}}"
            }
        )
    ) {
        SessionsRoute(
            navigateToSession = navigateToSession,
            columnState = it.columnState
        )
    }
}