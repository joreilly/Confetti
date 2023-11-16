package dev.johnoreilly.confetti.wear.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LteMobiledata
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.material.ToggleChip
import com.google.android.horologist.compose.material.ToggleChipToggleControl
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.proto.NetworkDetail
import dev.johnoreilly.confetti.wear.proto.WearPreferences
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
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
    columnState: ScalingLazyColumnState,
    updatePreferences: (WearPreferences) -> Unit
) {

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        columnState = columnState,
    ) {
        item {
            SectionHeader(text = stringResource(id = R.string.settings))
        }

        item {
            Chip(
                label = stringResource(R.string.settings_change_conference),
                onClick = conferenceCleared,
            )
        }

        item {
            Chip(
                label = stringResource(R.string.settings_refresh),
                onClick = onRefreshClick,
            )
        }

        if (uiState is SettingsUiState.Success) {
            item {
                val authUser = (uiState as? SettingsUiState.Success)?.authUser
                if (authUser == null) {
                    Chip(
                        label = stringResource(R.string.settings_sign_in),
                        onClick = navigateToGoogleSignIn,
                    )
                } else {
                    val clickActionLabel = stringResource(R.string.settings_action_sign_out)
                    val chipContentDescription = stringResource(
                        R.string.settings_sign_out_chip_content_description,
                        authUser.displayName ?: stringResource(R.string.settings_empty_name)
                    )
                    Chip(
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = chipContentDescription
                            onClick(clickActionLabel) {
                                navigateToGoogleSignOut()
                                true
                            }
                        },
                        label = stringResource(R.string.settings_sign_out),
                        icon = authUser.avatarUri,
                        largeIcon = true,
                        onClick = navigateToGoogleSignOut
                    )
                }
            }

            val wearPreferences = uiState.wearPreferences
            val networkPreferences = wearPreferences?.networkPreferences
            item {
                ToggleChip(
                    label = stringResource(R.string.settings_prefer_wifi),
                    icon = Icons.Default.Wifi,
                    checked = networkPreferences?.preferWifi ?: false,
                    onCheckedChanged = {
                        if (wearPreferences != null) {
                            updatePreferences(
                                wearPreferences.copy(
                                    networkPreferences = wearPreferences.networkPreferences?.copy(
                                        preferWifi = it
                                    )
                                )
                            )
                        }
                    },
                    toggleControl = ToggleChipToggleControl.Switch,
                    enabled = networkPreferences != null
                )
            }

            item {
                ToggleChip(
                    label = stringResource(R.string.settings_allow_lte),
                    icon = Icons.Default.LteMobiledata,
                    checked = networkPreferences?.allowLte ?: false,
                    onCheckedChanged = {
                        if (wearPreferences != null) {
                            updatePreferences(
                                wearPreferences.copy(
                                    networkPreferences = wearPreferences.networkPreferences?.copy(
                                        allowLte = it
                                    )
                                )
                            )
                        }
                    },
                    toggleControl = ToggleChipToggleControl.Switch,
                    enabled = networkPreferences != null
                )
            }

            item {
                ToggleChip(
                    label = stringResource(R.string.settings_show_networks),
                    icon = Icons.Default.NetworkPing,
                    checked = wearPreferences?.showNetworks == NetworkDetail.NETWORK_DETAIL_NETWORKS_AND_DATA,
                    onCheckedChanged = {
                        if (wearPreferences != null) {
                            updatePreferences(
                                wearPreferences.copy(
                                    showNetworks = if (it) {
                                        NetworkDetail.NETWORK_DETAIL_NETWORKS_AND_DATA
                                    } else {
                                        NetworkDetail.NETWORK_DETAIL_NONE
                                    }
                                )
                            )
                        }
                    },
                    toggleControl = ToggleChipToggleControl.Switch,
                    enabled = networkPreferences != null
                )
            }

            item {
                var developerModeCount by remember { mutableIntStateOf(0) }
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
                    text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
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
            Chip(label = "Refresh Token", onClick = onRefreshToken)
        }
    }
}

@Composable
private fun Long.localTime(): LocalDateTime =
    Instant.ofEpochSecond(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun SettingsListViewPreview() {
    ConfettiTheme {
        SettingsListView(
            conferenceCleared = {},
            columnState = ScalingLazyColumnDefaults.responsive().create(),
            navigateToGoogleSignIn = { },
            navigateToGoogleSignOut = { },
            uiState = SettingsUiState.Success(null),
            onRefreshClick = {},
            onRefreshToken = {},
            onEnableDeveloperMode = {},
            updatePreferences = {}
        )
    }
}
