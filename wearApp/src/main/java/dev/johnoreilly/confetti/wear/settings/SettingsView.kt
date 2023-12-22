package dev.johnoreilly.confetti.wear.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState

@Composable
fun SettingsRoute(
    component: SettingsComponent,
    columnState: ScalingLazyColumnState = rememberColumnState(),
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(scrollState = columnState) {
        SettingsListView(
            uiState = uiState,
            conferenceCleared = component::onSwitchConferenceSelected,
            navigateToGoogleSignIn = component::navigateToGoogleSignIn,
            navigateToGoogleSignOut = component::navigateToGoogleSignOut,
            onRefreshClick = {
                component.refresh()
            },
            onEnableDeveloperMode = {
                component.enableDeveloperMode()
            },
            onRefreshToken = {
                component.refreshToken()
            },
            columnState = columnState,
            updatePreferences = component::updatePreferences
        )
    }
}


