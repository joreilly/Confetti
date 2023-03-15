@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.ui


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.window.layout.DisplayFeature
import dev.johnoreilly.confetti.AppSettings.Companion.CONFERENCE_NOT_SET
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.account.navigation.SignInDestination
import dev.johnoreilly.confetti.account.navigation.signInGraph
import dev.johnoreilly.confetti.conferences.navigation.ConferencesDestination
import dev.johnoreilly.confetti.conferences.navigation.conferencesGraph
import dev.johnoreilly.confetti.navigation.TopLevelDestination
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsDestination
import dev.johnoreilly.confetti.sessiondetails.navigation.sessionDetailsGraph
import dev.johnoreilly.confetti.sessions.navigation.SessionsDestination
import dev.johnoreilly.confetti.sessions.navigation.sessionsGraph
import dev.johnoreilly.confetti.speakerdetails.navigation.SpeakerDetailsDestination
import dev.johnoreilly.confetti.speakerdetails.navigation.speakerDetailsGraph
import dev.johnoreilly.confetti.speakers.navigation.speakersGraph
import org.koin.androidx.compose.get

@Composable
fun ConfettiApp(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
) {
    val appState: ConfettiAppState = rememberConfettiAppState(
        windowSizeClass,
        displayFeatures,
        navController
    )

    val repository: ConfettiRepository = get()
    val conference by repository.getConferenceFlow()
        .collectAsState(initial = null)

    if (conference == null) {
        // Reading from the settings
        CircularProgressIndicator()
    } else {
        val initialRoute = if (conference == CONFERENCE_NOT_SET) {
            ConferencesDestination.route
        } else {
            SessionsDestination.route
        }
        NavHost(
            navController = navController,
            startDestination = initialRoute,
        ) {
            conferencesGraph {
                appState.navigate(
                    SessionsDestination,
                    SessionsDestination.createNavigationRoute(it)
                )
            }
            sessionsGraph(
                isExpandedScreen = appState.isExpandedScreen,
                displayFeatures = displayFeatures,
                conference = conference!!,
                navigateToSession = {
                    appState.navigate(
                        SessionDetailsDestination,
                        SessionDetailsDestination.createNavigationRoute(it)
                    )
                },
                navigateToSignIn = {
                    appState.navigate(
                        SignInDestination,
                        SignInDestination.route
                    )
                },
                onSignOut = {
                    appState.navigate(
                        ConferencesDestination, null
                    )
                },
                onSwitchConferenceSelected = {
                    appState.navigate(
                        ConferencesDestination, null
                    )
                }
            )
            sessionDetailsGraph(appState::onBackClick)
            speakersGraph(appState.isExpandedScreen,
                navigateToSpeaker = {
                    appState.navigate(
                        SpeakerDetailsDestination,
                        SpeakerDetailsDestination.createNavigationRoute(it)
                    )
                }
            )
            speakerDetailsGraph(appState::onBackClick)
            signInGraph(appState::onBackClick)
        }
    }
}

@Composable
private fun ConfettiScafold(
    appState: ConfettiAppState
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (appState.shouldShowBottomBar) {
                ConfettiBottomBar(
                    destinations = appState.topLevelDestinations,
                    onNavigateToDestination = appState::navigate,
                    currentDestination = appState.currentDestination
                )
            }
        }
    ) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal
                    )
                )
        ) {
            if (appState.shouldShowNavRail) {
                ConfettiNavRail(
                    destinations = appState.topLevelDestinations,
                    onNavigateToDestination = appState::navigate,
                    currentDestination = appState.currentDestination,
                    modifier = Modifier.safeDrawingPadding()
                )
            }


        }
    }
}

@Composable
private fun ConfettiNavRail(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {

    NavigationRail(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = ConfettiNavigationDefaults.navigationContentColor(),
    ) {
        destinations.forEach { destination ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
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


@Composable
private fun ConfettiBottomBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?
) {
    NavigationBar(
        contentColor = ConfettiNavigationDefaults.navigationContentColor(),
        tonalElevation = 0.dp,
    ) {
        destinations.forEach { destination ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
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

object ConfettiNavigationDefaults {
    @Composable
    fun navigationContentColor() = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun navigationSelectedItemColor() = MaterialTheme.colorScheme.onPrimaryContainer

    @Composable
    fun navigationIndicatorColor() = MaterialTheme.colorScheme.primaryContainer
}

