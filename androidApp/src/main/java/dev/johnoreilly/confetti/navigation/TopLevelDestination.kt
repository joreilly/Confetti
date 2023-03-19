package dev.johnoreilly.confetti.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import dev.johnoreilly.confetti.R
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
}

object SessionsTopLevelDestination: TopLevelDestination {
    override val selectedIcon = Icons.Filled.CalendarMonth
    override val unselectedIcon = Icons.Outlined.CalendarMonth
    override val iconTextId = R.string.schedule
    override val routePattern = sessionsRoutePattern

    override fun route(conference: String): String {
        return SessionsKey(conference).route
    }
}

object SpeakersTopLevelDestination: TopLevelDestination {
    override val selectedIcon = Icons.Filled.Person
    override val unselectedIcon = Icons.Outlined.Person
    override val iconTextId = R.string.speakers
    override val routePattern = speakersRoutePattern

    override fun route(conference: String): String {
        return SpeakersKey(conference).route
    }
}

internal fun String.urlEncoded() = Uri.encode(this)
internal fun String.urlDecoded() = Uri.decode(this)
