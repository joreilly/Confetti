@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.bookmarks.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksRoute
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination

object BookmarksDestination : ConfettiNavigationDestination {
    const val conferenceArg = "conference"
    override val route = "bookmarks_route/{$conferenceArg}"
    override val destination = "bookmarks_destination"

    fun createNavigationRoute(conference: String): String {
        return "bookmarks_route/${conference}"
    }

    fun fromNavArgs(entry: NavBackStackEntry): String {
        val arguments = entry.arguments!!
        return "" //arguments.getString(SessionDetailsDestination.conferenceArg)!!
    }

    fun fromNavArgs(savedStateHandle: SavedStateHandle): String {
        return savedStateHandle[conferenceArg]!!
    }
}

fun NavGraphBuilder.bookmarksGraph(
    navigateToSession: (SessionDetailsKey) -> Unit
) {
    scrollable(
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
            columnState = it.columnState
        )
    }
}