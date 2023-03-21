package dev.johnoreilly.confetti.ui

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import androidx.window.layout.DisplayFeature

@Composable
fun rememberConfettiAppState(
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    navController: NavHostController,
): ConfettiAppState {
    return remember(navController, windowSizeClass, displayFeatures) {
        ConfettiAppState(navController, windowSizeClass, displayFeatures)
    }
}

@Stable
class ConfettiAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass,
    val displayFeatures: List<DisplayFeature>
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    var shouldShowSettingsDialog by mutableStateOf(false)
        private set

    val shouldShowBottomBar: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact ||
            windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

    val shouldShowNavRail: Boolean
        get() = !shouldShowBottomBar

    val isExpandedScreen: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    /**
     * UI logic for navigating to a top level destination in the app. Top level destinations have
     * only one copy of the destination of the back stack, and save and restore state whenever you
     * navigate to and from it.
     */
    fun navigateToTopLevelDestination(route: String) {
        val topLevelNavOptions = navOptions {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }

        navController.navigate(route, topLevelNavOptions)
    }

    fun navigate(route: String) {
        navController.navigate(route)
    }

    fun onBackClick() {
        navController.popBackStack()
    }

    fun setShowSettingsDialog(shouldShow: Boolean) {
        shouldShowSettingsDialog = shouldShow
    }

}

