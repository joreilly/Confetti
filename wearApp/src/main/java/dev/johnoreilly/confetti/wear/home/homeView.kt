@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.wear.ConfettiViewModel
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeRoute(
    navigateToSession: (String) -> Unit,
    navigateToDay: (LocalDate) -> Unit,
    navigateToSettings: () -> Unit,
    columnState: ScalingLazyColumnState,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeListView(
        uiState = uiState,
        sessionSelected = navigateToSession,
        daySelected = navigateToDay,
        onSettingsClick = navigateToSettings,
        columnState = columnState,
    )
}


