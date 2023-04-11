package dev.johnoreilly.confetti.sessions.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import dev.johnoreilly.confetti.navigation.urlDecoded
import dev.johnoreilly.confetti.navigation.urlEncoded
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.sessions.HomeUiState
import dev.johnoreilly.confetti.sessions.HomeViewModel
import dev.johnoreilly.confetti.sessions.SessionsRoute
import dev.johnoreilly.confetti.ui.ConfettiAppState
import org.koin.androidx.compose.koinViewModel

private const val base = "sessions"
private const val conferenceArg = "conference"

val sessionsRoutePattern = "$base/{$conferenceArg}"

class SessionsKey(val conference: String) {
    constructor(backStackEntry: NavBackStackEntry) :
        this(backStackEntry.arguments!!.getString(conferenceArg)!!.urlDecoded())

    val route: String = "${base}/${conference.urlEncoded()}"

    companion object {
        fun conferenceFromNavArgs(savedStateHandle: SavedStateHandle): String? {
            return savedStateHandle[conferenceArg]
        }
    }
}


fun NavGraphBuilder.sessionsGraph(
    appState: ConfettiAppState,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSwitchConferenceSelected: () -> Unit,
    defaultConferenceParameter: String?,
) {
    composable(
        route = sessionsRoutePattern,
        arguments = listOf(
            navArgument(conferenceArg) {
                type = NavType.StringType
                nullable = true
                defaultValue = defaultConferenceParameter
            }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern = "confetti://confetti/sessions/{$conferenceArg}"
            }
        )
    ) {
        val homeViewModel: HomeViewModel = koinViewModel()
        when (val homeUiState = homeViewModel.uiState.collectAsStateWithLifecycle().value) {
            HomeUiState.Loading -> {}
            HomeUiState.NoConference -> {
                LaunchedEffect(Unit) {
                    onSwitchConferenceSelected()
                }
            }
            is HomeUiState.Conference -> {
                SessionsRoute(
                    conference = homeUiState.conference,
                    appState = appState,
                    navigateToSession = navigateToSession,
                    onSignOut = onSignOut,
                    navigateToSignIn = navigateToSignIn,
                    onSwitchConferenceSelected = onSwitchConferenceSelected,
                )
            }
        }
    }
}
