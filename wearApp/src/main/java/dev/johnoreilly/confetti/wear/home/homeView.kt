@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.home

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.wear.conferences.ConferencesUiState
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeRoute(
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDayKey) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToConferenceList: () -> Unit,
    columnState: ScalingLazyColumnState,
    viewModel: HomeViewModel = getViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    ReportDrawnWhen {
        uiState !is HomeUiState.Loading
    }

    SideEffect {
        if (uiState is HomeUiState.NoneSelected) {
            navigateToConferenceList()
        }
    }

    HomeListView(
        uiState = uiState,
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
        onRefreshClick = {
            viewModel.refresh()
        },
        columnState = columnState,
    )
}
