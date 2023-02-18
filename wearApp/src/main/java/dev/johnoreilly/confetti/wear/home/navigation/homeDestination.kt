@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.home.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import com.google.android.horologist.compose.navscaffold.scrollable
import dev.johnoreilly.confetti.wear.home.HomeRoute
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import kotlinx.datetime.LocalDate

object HomeDestination : ConfettiNavigationDestination {
    override val route = "home_route"
    override val destination = "home_destination"
}

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.homeGraph(
    navigateToDay: (LocalDate) -> Unit,
    ) {
    scrollable(route = HomeDestination.route) {
        HomeRoute(
            columnState = it.columnState,
            navigateToDay = navigateToDay
        )
    }
}
