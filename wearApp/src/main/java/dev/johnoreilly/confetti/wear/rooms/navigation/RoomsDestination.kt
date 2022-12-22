@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.rooms.navigation

import androidx.navigation.NavGraphBuilder
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.rooms.RoomsRoute

object RoomsDestination : ConfettiNavigationDestination {
    override val route = "rooms_route"
    override val destination = "rooms_destination"
}

fun NavGraphBuilder.roomsGraph() {
    scrollable(route = RoomsDestination.route) {
        RoomsRoute(it.columnState)
    }
}
