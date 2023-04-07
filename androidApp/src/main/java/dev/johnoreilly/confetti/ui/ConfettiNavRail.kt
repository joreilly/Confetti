package dev.johnoreilly.confetti.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import dev.johnoreilly.confetti.navigation.TopLevelDestination


@Composable
internal fun ConfettiNavRail(
    conference: String,
    onNavigateToDestination: (String) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {

    NavigationRail(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = ConfettiNavigationDefaults.navigationContentColor(),
    ) {
        TopLevelDestination.values.forEach { destination ->
            val route = destination.route(conference)
            val selected =
                currentDestination?.hierarchy?.any { it.route == destination.routePattern } == true
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigateToDestination(route) },
                icon = {
                    val icon = if (selected) {
                        destination.selectedIcon
                    } else {
                        destination.unselectedIcon
                    }
                    Icon(icon, contentDescription = stringResource(destination.iconTextId))
                }
            )
        }
    }
}

object ConfettiNavigationDefaults {
    @Composable
    fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant
    @Composable
    fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer
    @Composable
    fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}
