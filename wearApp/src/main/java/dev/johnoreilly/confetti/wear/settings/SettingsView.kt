package dev.johnoreilly.confetti.wear.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsRoute(
    component: SettingsComponent,
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

    SettingsListView(
        uiState = uiState,
        conferenceCleared = component::onSwitchConferenceSelected,
        onRefreshClick = {
            component.refresh()
        },
        onEnableDeveloperMode = {
            component.enableDeveloperMode()
        },
        onSignIn = { component.signIn() },
        onSignOut = { component.signOut() },
        updatePreferences = component::updatePreferences
    )
}


