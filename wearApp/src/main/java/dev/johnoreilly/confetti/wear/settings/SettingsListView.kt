@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.base.ui.components.StandardChip
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun SettingsListView(
    uiState: SettingsUiState,
    conferenceCleared: () -> Unit,
    navigateToGoogleSignIn: () -> Unit,
    navigateToGoogleSignOut: () -> Unit,
    onRefreshClick: () -> Unit,
    onRefreshToken: () -> Unit,
    onEnableDeveloperMode: () -> Unit,
    columnState: ScalingLazyColumnState
) {

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        item {
            SectionHeader(text = stringResource(id = R.string.settings))
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

        if (uiState is SettingsUiState.Success) {
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

            item {
                var developerModeCount by remember { mutableStateOf(0) }
                Text(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .run {
                            if (!uiState.developerMode) {
                                clickable {
                                    developerModeCount++
                                    if (developerModeCount > 8) {
                                        onEnableDeveloperMode()
                                    }
                                }
                            } else {
                                this
                            }
                        },
                    text = "Version: ${BuildConfig.VERSION_NAME}",
                )
            }

            if (uiState.developerMode) {
                developerModeOptions(uiState, onRefreshToken)
            }
        }
    }
}

private fun ScalingLazyListScope.developerModeOptions(
    uiState: SettingsUiState.Success,
    onRefreshToken: () -> Unit
) {
    val firebaseUser = uiState.firebaseUser
    val token = uiState.token

    item {
        Text(
            text = "Developer Mode",
        )
    }

    if (firebaseUser != null) {
        item {
            Text(
                style = MaterialTheme.typography.caption3,
                text = "Email: ${firebaseUser.email}"
            )
        }

        if (token != null) {
            item {
                Text(
                    style = MaterialTheme.typography.caption3,
                    text = "Provider: ${token.signInProvider}"
                )
            }
            item {
                Text(
                    style = MaterialTheme.typography.caption3,
                    text = "Expires: ${token.expirationTimestamp.localTime()}"
                )
            }
            item {
                Text(
                    style = MaterialTheme.typography.caption3,
                    text = "Issued: ${token.issuedAtTimestamp.localTime()}"
                )
            }
        }

        item {
            StandardChip(label = "Refresh Token", onClick = onRefreshToken)
        }
    }
}

@Composable
private fun Long.localTime(): LocalDateTime =
    Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

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
            onRefreshClick = {},
            onRefreshToken = {},
            onEnableDeveloperMode = {}
        )
    }
}
