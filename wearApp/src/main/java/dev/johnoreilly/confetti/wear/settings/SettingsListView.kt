@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.base.ui.components.StandardChip
import com.google.android.horologist.composables.PlaceholderChip
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.data.auth.AuthSource
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes

@Composable
fun SettingsListView(
    uiState: SettingsUiState,
    conferenceCleared: () -> Unit,
    navigateToGoogleSignIn: () -> Unit,
    navigateToGoogleSignOut: () -> Unit,
    columnState: ScalingLazyColumnState
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        item {
            SectionHeader(text = "Settings")
        }

        item {
            StandardChip(
                label = "Change Conference",
                onClick = conferenceCleared,
            )
        }

        item {
            when (uiState) {
                is SettingsUiState.Loading -> {
                    PlaceholderChip()
                }

                is SettingsUiState.Success -> {
                    val authAndSource = uiState.authAndSource
                    if (authAndSource == null) {
                        StandardChip(
                            label = "Sign In",
                            onClick = navigateToGoogleSignIn,
                        )
                    } else if (authAndSource.source == AuthSource.GoogleSignIn) {
                        StandardChip(
                            modifier = Modifier.clearAndSetSemantics {
                                contentDescription =
                                    "Logged in as " + authAndSource.user.displayName
                                onClick("Sign Out") {
                                    navigateToGoogleSignOut()
                                    true
                                }
                            },
                            label = "Sign Out",
                            icon = authAndSource.user.avatarUri,
                            largeIcon = true,
                            onClick = navigateToGoogleSignOut
                        )
                    } else {
                        StandardChip(
                            enabled = false,
                            label = authAndSource.user.displayName ?: "Signed In",
                            icon = authAndSource.user.avatarUri,
                            largeIcon = true,
                            onClick = navigateToGoogleSignOut
                        )
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun SettingsListViewPreview() {
    ConfettiTheme {
        SettingsListView(
            conferenceCleared = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            navigateToGoogleSignIn = { },
            navigateToGoogleSignOut = { },
            uiState = SettingsUiState.Success(null)
        )
    }
}
