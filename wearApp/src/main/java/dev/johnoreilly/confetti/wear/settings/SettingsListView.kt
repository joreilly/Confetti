package dev.johnoreilly.confetti.wear.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LteMobiledata
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import coil.compose.AsyncImage
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.wear.components.ScreenHeader
import dev.johnoreilly.confetti.wear.proto.NetworkDetail
import dev.johnoreilly.confetti.wear.proto.NetworkPreferences
import dev.johnoreilly.confetti.wear.proto.WearPreferences
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun SettingsListView(
    uiState: SettingsUiState,
    conferenceCleared: () -> Unit,
    onRefreshClick: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onEnableDeveloperMode: () -> Unit,
    updatePreferences: (WearPreferences) -> Unit,
    columnState: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
) {
    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = rememberResponsiveColumnPadding(
                first = ColumnItemType.ListHeader,
                last = ColumnItemType.Button
            ),
        ) {
            item {
                ScreenHeader(
                    modifier = Modifier,
                    text = stringResource(id = R.string.settings)
                )
            }

            item {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = conferenceCleared,
                ) {
                    Text(stringResource(R.string.settings_change_conference))
                }
            }

            item {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = onRefreshClick,
                ) {
                    Text(stringResource(R.string.settings_refresh))
                }
            }

            if (uiState is SettingsUiState.Success) {
                val wearPreferences = uiState.wearPreferences
                val networkPreferences = wearPreferences?.networkPreferences

                item {
                    val authUser = uiState.authUser
                    if (authUser == null) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = onSignIn,
                        ) {
                            Text(stringResource(R.string.settings_sign_in))
                        }
                    } else {
                        val clickActionLabel = stringResource(R.string.settings_action_sign_out)
                        val chipContentDescription = stringResource(
                            R.string.settings_sign_out_chip_content_description,
                            authUser.name
                        )
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clearAndSetSemantics {
                                    contentDescription = chipContentDescription
                                    onClick(clickActionLabel) {
                                        onSignOut()
                                        true
                                    }
                                },
                            icon = { AsyncImage(model = authUser.photoUrl, contentDescription = null) },
                            onClick = { onSignOut() }
                        ) {
                            Text(stringResource(R.string.settings_sign_out))
                        }
                    }
                }

                item {
                    SwitchButton(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = { Text(stringResource(R.string.settings_allow_lte)) },
                        icon = {
                            androidx.wear.compose.material3.Icon(
                                imageVector = Icons.Default.LteMobiledata,
                                contentDescription = null
                            )
                        },
                        checked = networkPreferences?.allowLte ?: false,
                        onCheckedChange = {
                            if (wearPreferences != null) {
                                updatePreferences(
                                    wearPreferences.copy(
                                        networkPreferences = (wearPreferences.networkPreferences
                                            ?: NetworkPreferences()).copy(
                                            allowLte = it
                                        )
                                    )
                                )
                            }
                        },
                        enabled = wearPreferences != null
                    )
                }

                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = {
                            Text(
                                when (wearPreferences?.showNetworks) {
                                    NetworkDetail.NETWORK_DETAIL_NETWORKS_AND_DATA -> stringResource(R.string.settings_show_networks_and_data)
                                    NetworkDetail.NETWORK_DETAIL_NETWORKS -> stringResource(R.string.settings_show_networks)
                                    else -> stringResource(R.string.settings_hide_networks)
                                }
                            )
                        },
                        icon = {
                            androidx.wear.compose.material3.Icon(
                                imageVector = Icons.Default.NetworkPing,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            if (wearPreferences != null) {
                                updatePreferences(
                                    wearPreferences.copy(
                                        showNetworks = when (wearPreferences.showNetworks) {
                                            NetworkDetail.NETWORK_DETAIL_NETWORKS_AND_DATA -> NetworkDetail.NETWORK_DETAIL_NETWORKS
                                            NetworkDetail.NETWORK_DETAIL_NETWORKS -> NetworkDetail.NETWORK_DETAIL_NONE
                                            else -> NetworkDetail.NETWORK_DETAIL_NETWORKS_AND_DATA
                                        }
                                    )
                                )
                            }
                        },
                        enabled = wearPreferences != null
                    )
                }

                item {
                    var developerModeCount by remember { mutableIntStateOf(0) }
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
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
                    developerModeOptions(uiState)
                }
            }
        }
    }
}

private fun TransformingLazyColumnScope.developerModeOptions(
    uiState: SettingsUiState.Success,
) {
    val authUser = uiState.authUser

    item {
        ListSubHeader(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                text = "Developer Mode",
            )
        }
    }

    if (authUser != null) {
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                text = "Email: ${authUser.email}"
            )
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
    SettingsListView(
        conferenceCleared = {},
        onSignIn = { },
        onSignOut = { },
        uiState = SettingsUiState.Success(),
        onRefreshClick = {},
        onEnableDeveloperMode = {},
        updatePreferences = {}
    )
}
