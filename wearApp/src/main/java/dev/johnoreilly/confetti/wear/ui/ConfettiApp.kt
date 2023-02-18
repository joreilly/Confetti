package dev.johnoreilly.confetti.wear.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.google.android.horologist.compose.navscaffold.WearNavScaffold
import dev.johnoreilly.confetti.wear.home.navigation.HomeDestination
import dev.johnoreilly.confetti.wear.home.navigation.homeGraph
import dev.johnoreilly.confetti.wear.navigation.ConfettiNavigationDestination
import dev.johnoreilly.confetti.wear.sessions.navigation.SessionsDestination
import dev.johnoreilly.confetti.wear.sessions.navigation.sessionsGraph
import dev.johnoreilly.confetti.wear.settings.navigation.SettingsDestination
import dev.johnoreilly.confetti.wear.settings.navigation.settingsGraph

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConfettiApp(navController: NavHostController) {
    fun onNavigateToDestination(destination: ConfettiNavigationDestination, route: String? = null) {
        navController.navigate(route ?: destination.route)
    }

    fun onBackClick() {
        navController.popBackStack()
    }

    WearNavScaffold(startDestination = HomeDestination.route, navController = navController) {
        homeGraph(
            navigateToDay = {
                onNavigateToDestination(
                    SessionsDestination,
                    SessionsDestination.createNavigationRoute(it)
                )
            }
        )

        sessionsGraph(
            navigateToSession = {
                onNavigateToDestination(
                )
            },
        )

    }
}

fun onNavigateToDestination() {
}
