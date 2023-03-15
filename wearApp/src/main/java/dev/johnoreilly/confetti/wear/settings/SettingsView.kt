@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import org.koin.androidx.compose.getViewModel

@Composable
fun SettingsRoute(
    columnState: ScalingLazyColumnState,
    onSwitchConferenceSelected: () -> Unit,
    navigateToGoogleSignIn: () -> Unit,
    navigateToGoogleSignOut: () -> Unit,
    viewModel: SettingsViewModel = getViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsListView(
        uiState = uiState,
        conferenceCleared = {
            viewModel.clearConference()
            onSwitchConferenceSelected()
        },
        navigateToGoogleSignIn = navigateToGoogleSignIn,
        navigateToGoogleSignOut = navigateToGoogleSignOut,
        columnState = columnState
    )
}


