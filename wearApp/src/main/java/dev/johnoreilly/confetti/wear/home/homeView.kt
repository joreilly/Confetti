@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.navigation.ConferenceDayKey
import dev.johnoreilly.confetti.navigation.SessionDetailsKey
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeRoute(
    conference: String,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (ConferenceDayKey) -> Unit,
    navigateToSettings: () -> Unit,
    columnState: ScalingLazyColumnState,
    viewModel: HomeViewModel = getViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeListView(
        uiState = uiState,
        sessionSelected = {
            navigateToSession(SessionDetailsKey(conference, it))
        },
        daySelected = { navigateToDay(ConferenceDayKey(conference, it)) },
        onSettingsClick = navigateToSettings,
        onRefreshClick = {
            viewModel.refresh()
        },
        columnState = columnState,
    )
}


