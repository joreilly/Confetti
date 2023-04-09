@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.home

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.utils.QueryResult
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeRoute(
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDayKey) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToBookmarks: (String) -> Unit,
    columnState: ScalingLazyColumnState,
    viewModel: HomeViewModel = getViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val bookmarksUiState by viewModel.bookmarksUiState.collectAsStateWithLifecycle()

    if (!BuildConfig.DEBUG) {
        ReportDrawnWhen {
            uiState !is QueryResult.Loading && bookmarksUiState !is QueryResult.Loading
        }
    }

    HomeScreen(
        uiState = uiState,
        bookmarksUiState = bookmarksUiState,
        sessionSelected = {
            if (uiState is QueryResult.Success) {
                navigateToSession(SessionDetailsKey(uiState.result.conference, it))
            }
        },
        daySelected = {
            if (uiState is QueryResult.Success) {
                navigateToDay(ConferenceDayKey(uiState.result.conference, it))
            }
        },
        onSettingsClick = navigateToSettings,
        onBookmarksClick = {
            if (uiState is QueryResult.Success) {
                navigateToBookmarks(uiState.result.conference)
            }
        },
        columnState = columnState,
    )
}
