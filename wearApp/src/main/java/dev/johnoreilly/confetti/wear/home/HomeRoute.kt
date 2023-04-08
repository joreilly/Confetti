@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.home

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksUiState
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeRoute(
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDayKey) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToBookmarks: (String) -> Unit,
    columnState: ScalingLazyColumnState,
) {
    val viewModel: HomeViewModel = getViewModel()

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val bookmarksUiState by viewModel.bookmarksUiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState,
        bookmarksUiState,
        navigateToSession,
        navigateToDay,
        navigateToSettings,
        navigateToBookmarks,
        columnState
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    bookmarksUiState: BookmarksUiState,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDayKey) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToBookmarks: (String) -> Unit,
    columnState: ScalingLazyColumnState
) {
    if (!BuildConfig.DEBUG) {
        ReportDrawnWhen {
            uiState !is HomeUiState.Loading
        }
    }

    HomeScreen(
        uiState = uiState,
        bookmarksUiState = bookmarksUiState,
        sessionSelected = {
            if (uiState is HomeUiState.Success) {
                navigateToSession(SessionDetailsKey(uiState.conference, it))
            }
        },
        daySelected = {
            if (uiState is HomeUiState.Success) {
                navigateToDay(ConferenceDayKey(uiState.conference, it))
            }
        },
        onSettingsClick = navigateToSettings,
        onBookmarksClick = {
            if (uiState is HomeUiState.Success) {
                navigateToBookmarks(uiState.conference)
            }
        },
        columnState = columnState,
    )
}
