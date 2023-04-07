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
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes

@Composable
fun SettingsListView(
    uiState: SettingsUiState,
    conferenceCleared: () -> Unit,
    navigateToGoogleSignIn: () -> Unit,
    navigateToGoogleSignOut: () -> Unit,
    onRefreshClick: () -> Unit,
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
            StandardChip(
                label = "Refresh",
                onClick = onRefreshClick,
            )
        }

        item {
            val authUser = (uiState as? SettingsUiState.Success)?.authUser
            if (authUser == null) {
                StandardChip(
                    label = "Sign In",
                    onClick = navigateToGoogleSignIn,
                )
            } else {
                StandardChip(
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "Logged in as " + authUser.displayName
                        onClick("Sign Out") {
                            navigateToGoogleSignOut()
                            true
                        }
                    },
                    label = "Sign Out",
                    icon = authUser.avatarUri,
                    largeIcon = true,
                    onClick = navigateToGoogleSignOut
                )
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
            uiState = SettingsUiState.Success(null),
            onRefreshClick = {}
        )
    }
}
