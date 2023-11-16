package dev.johnoreilly.confetti.wear.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.horologist.compose.layout.ScalingLazyColumnState

@Composable
fun SettingsRoute(
    component: SettingsComponent,
    columnState: ScalingLazyColumnState,
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

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


