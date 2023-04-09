@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.startup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.utils.QueryResult
import dev.johnoreilly.confetti.wear.home.HomeScreen
import org.koin.androidx.compose.getViewModel

// FIXME: use https://developer.android.com/develop/ui/views/launch/splash-screen#suspend-drawing to avoid the progressbar
@Composable
fun StartHomeRoute(
    columnState: ScalingLazyColumnState,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDayKey) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToBookmarks: (String) -> Unit,
    navigateToConferences: () -> Unit,
) {
    val viewModel: StartViewModel = getViewModel()

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val bookmarksUiState by viewModel.bookmarksUiState.collectAsStateWithLifecycle()

    if (uiState is QueryResult.None) {
        val lifecycle = LocalLifecycleOwner.current
        LaunchedEffect(uiState) {
            // If we don't have a conference to show,
            // and we are the top of the navigation stack
            // then send the user to select a conference
            lifecycle.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
                navigateToConferences()
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        bookmarksUiState = bookmarksUiState,
        columnState = columnState,
        daySelected = navigateToDay,
        onBookmarksClick = navigateToBookmarks,
        onSettingsClick = navigateToSettings,
        sessionSelected = {
            navigateToSession(it)
        }
    )
}