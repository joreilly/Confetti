package dev.johnoreilly.confetti.sessions.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.johnoreilly.confetti.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.rooms.RoomsRoute

object RoomsDestination : ConfettiNavigationDestination {
    override val route = "rooms_route"
    override val destination = "rooms_destination"
}

fun NavGraphBuilder.roomsGraph() {
    composable(route = RoomsDestination.route) {
        RoomsRoute()
    }
}
