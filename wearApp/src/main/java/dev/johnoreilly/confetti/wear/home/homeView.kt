@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.wear.sessiondetails.navigation.SessionDetailsKey
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeRoute(
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToDay: (LocalDate) -> Unit,
    navigateToSettings: () -> Unit,
    columnState: ScalingLazyColumnState,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val refreshScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeListView(
        uiState = uiState,
        sessionSelected = {
            navigateToSession(SessionDetailsKey(viewModel.savedConference.value, it))
        },
        daySelected = navigateToDay,
        onSettingsClick = navigateToSettings,
        onRefreshClick = {
            refreshScope.launch {
                viewModel.refresh()
            }
        },
        columnState = columnState,
    )
}


