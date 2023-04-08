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
import dev.johnoreilly.confetti.wear.home.HomeScreenContent
import dev.johnoreilly.confetti.wear.home.HomeUiState
import org.koin.androidx.compose.getViewModel

// FIXME: use https://developer.android.com/develop/ui/views/launch/splash-screen#suspend-drawing to avoid the progressbar
@Composable
fun InitialLoadingRoute(
    columnState: ScalingLazyColumnState,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDayKey) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToBookmarks: (String) -> Unit,
    navigateToConferences: () -> Unit,
) {
    val viewModel: SplashViewModel = getViewModel()

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val bookmarksUiState by viewModel.bookmarksUiState.collectAsStateWithLifecycle()

    val lifecycle = LocalLifecycleOwner.current
    LaunchedEffect(uiState) {
        println("uiState $uiState")
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
            if (uiState is HomeUiState.NoConference) {
                navigateToConferences()
            }
        }
    }

    HomeScreenContent(
        uiState = uiState,
        bookmarksUiState = bookmarksUiState,
        navigateToSession = navigateToSession,
        navigateToDay = navigateToDay,
        navigateToSettings = navigateToSettings,
        navigateToBookmarks = navigateToBookmarks,
        columnState = columnState
    )
}