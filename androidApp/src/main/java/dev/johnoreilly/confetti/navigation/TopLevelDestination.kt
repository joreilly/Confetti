package dev.johnoreilly.confetti.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.bookmarks.navigation.BookmarksKey
import dev.johnoreilly.confetti.bookmarks.navigation.bookmarksRoutePattern
import dev.johnoreilly.confetti.search.navigation.SearchKey
import dev.johnoreilly.confetti.search.navigation.searchRoutePattern
import dev.johnoreilly.confetti.sessions.navigation.SessionsKey
import dev.johnoreilly.confetti.sessions.navigation.sessionsRoutePattern
import dev.johnoreilly.confetti.speakers.navigation.SpeakersKey
import dev.johnoreilly.confetti.speakers.navigation.speakersRoutePattern

sealed interface TopLevelDestination {
    val selectedIcon: ImageVector
    val unselectedIcon: ImageVector
    val iconTextId: Int

    // FIXME: do we need both routePattern and route?
    val routePattern: String
    fun route(conference: String): String

    companion object {

        // All current values, the order is how the tabs will appear in the app.
        val values = listOf(
            SessionsTopLevelDestination,
            SpeakersTopLevelDestination,
            BookmarksTopLevelDestination,
            SearchTopLevelDestination,
        )
    }
}

object SessionsTopLevelDestination : TopLevelDestination {
    override val selectedIcon = Icons.Filled.CalendarMonth
    override val unselectedIcon = Icons.Outlined.CalendarMonth
    override val iconTextId = R.string.schedule
    override val routePattern = sessionsRoutePattern

    override fun route(conference: String): String {
        return SessionsKey(conference).route
    }
}

object SpeakersTopLevelDestination : TopLevelDestination {
    override val selectedIcon = Icons.Filled.Person
    override val unselectedIcon = Icons.Outlined.Person
    override val iconTextId = R.string.speakers
    override val routePattern = speakersRoutePattern

    override fun route(conference: String): String {
        return SpeakersKey(conference).route
    }
}

object SearchTopLevelDestination : TopLevelDestination {
    override val selectedIcon = Icons.Filled.Search
    override val unselectedIcon = Icons.Outlined.Search
    override val iconTextId = R.string.search
    override val routePattern = searchRoutePattern

    override fun route(conference: String): String {
        return SearchKey(conference).route
    }
}

object BookmarksTopLevelDestination : TopLevelDestination {
    override val selectedIcon = Icons.Filled.Bookmarks
    override val unselectedIcon = Icons.Outlined.Bookmarks
    override val iconTextId = R.string.bookmarks
    override val routePattern = bookmarksRoutePattern

    override fun route(conference: String): String {
        return BookmarksKey(conference).route
    }
}

internal fun String.urlEncoded() = Uri.encode(this)
internal fun String.urlDecoded() = Uri.decode(this)
