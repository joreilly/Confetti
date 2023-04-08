@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.bookmarks.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import androidx.wear.compose.navigation.composable
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksRoute
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsDestination

object BookmarksDestination : ConfettiNavigationDestination {
    const val conferenceArg = "conference"
    override val route = "bookmarks_route/{$conferenceArg}"
    override val destination = "bookmarks_destination"

    fun createNavigationRoute(conference: String): String {
        return "bookmarks_route/${conference}"
    }

    fun fromNavArgs(entry: NavBackStackEntry): String {
        val arguments = entry.arguments!!
        return arguments.getString(SessionDetailsDestination.conferenceArg)!!
    }

    fun fromNavArgs(savedStateHandle: SavedStateHandle): String {
        return savedStateHandle[conferenceArg]!!
    }
}

fun NavGraphBuilder.bookmarksGraph(
    navigateToSession: (SessionDetailsKey) -> Unit
) {
    composable(
        route = BookmarksDestination.route,
        arguments = listOf(
            navArgument(BookmarksDestination.conferenceArg) { type = NavType.StringType }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern =
                    "confetti://confetti/bookmarks/{${BookmarksDestination.conferenceArg}}"
            }
        )
    ) {
        BookmarksRoute(
            navigateToSession = navigateToSession,
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}