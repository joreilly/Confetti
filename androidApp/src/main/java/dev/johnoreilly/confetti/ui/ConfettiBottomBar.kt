package dev.johnoreilly.confetti.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import dev.johnoreilly.confetti.navigation.TopLevelDestination

@Composable
internal fun ConfettiBottomBar(
    conference: String,
    onNavigateToDestination: (String) -> Unit,
    currentDestination: NavDestination?
) {
    NavigationBar(
        contentColor = ConfettiNavigationDefaults.navigationContentColor(),
    ) {
        TopLevelDestination.values.forEach { destination ->
            val route = destination.route(conference)

            val selected =
                currentDestination?.hierarchy?.any { it.route == destination.routePattern } == true
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(route) },
                icon = {
                    val icon = if (selected) {
                        destination.selectedIcon
                    } else {
                        destination.unselectedIcon
                    }
                    Icon(icon, contentDescription = stringResource(destination.iconTextId))
                },
                label = { Text(stringResource(destination.iconTextId)) }
            )
        }
    }
}
