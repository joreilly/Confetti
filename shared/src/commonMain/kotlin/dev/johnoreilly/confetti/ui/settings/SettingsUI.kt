@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.dark_mode_config_dark
import confetti.shared.generated.resources.dark_mode_config_light
import confetti.shared.generated.resources.dark_mode_config_system_default
import confetti.shared.generated.resources.dark_mode_preference
import confetti.shared.generated.resources.developerSettings
import confetti.shared.generated.resources.enable_notifications
import confetti.shared.generated.resources.settings_boolean_false
import confetti.shared.generated.resources.settings_boolean_true
import confetti.shared.generated.resources.settings_title
import confetti.shared.generated.resources.use_experimental_features
import dev.johnoreilly.confetti.decompose.DarkThemeConfig
import dev.johnoreilly.confetti.decompose.DeveloperSettings
import dev.johnoreilly.confetti.decompose.SettingsComponent
import dev.johnoreilly.confetti.decompose.UserEditableSettings
import dev.johnoreilly.confetti.permissions.rememberNotificationPermissionState
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsUI(
    component: SettingsComponent,
    popBack: () -> Unit
) {
    val userEditableSettings by component.userEditableSettings.collectAsStateWithLifecycle()
    val developerSettings by component.developerSettings.collectAsStateWithLifecycle()
    SettingsUI(
        userEditableSettings = userEditableSettings,
        onChangeDarkThemeConfig = component::updateDarkThemeConfig,
        onChangeUseExperimentalFeatures = component::updateUseExperimentalFeatures,
        developerSettings = developerSettings,
        onEnableDeveloperMode = component::enableDeveloperMode,
        onSendNotifications = component::sendNotifications,
        supportsNotifications = component.supportsNotifications,
        onNotificationsEnabled = component::updateNotificationsEnabled,
        popBack = popBack
    )
}

@Composable
fun SettingsUI(
    userEditableSettings: UserEditableSettings?,
    onChangeUseExperimentalFeatures: (value: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
    developerSettings: DeveloperSettings?,
    onEnableDeveloperMode: () -> Unit,
    onSendNotifications: () -> Unit,
    supportsNotifications: Boolean,
    onNotificationsEnabled: (value: Boolean) -> Unit,
    popBack: () -> Unit
) {
    val scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    /**
     * usePlatformDefaultWidth = false is use as a temporary fix to allow
     * height recalculation during recomposition. This, however, causes
     * Dialog's to occupy full width in Compact mode. Therefore max width
     * is configured below. This should be removed when there's fix to
     * https://issuetracker.google.com/issues/221643630
     */
    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { popBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) {
        Column(
            Modifier
                .clipToBounds()
                .padding(it)
        ) {
            HorizontalDivider(Modifier.padding(top = 8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        SettingsPanel(
                            settings = userEditableSettings,
                            onChangeDarkThemeConfig = onChangeDarkThemeConfig,
                            onChangeUseExperimentalFeatures = onChangeUseExperimentalFeatures,
                            onChangeNotificationsEnabled = onNotificationsEnabled,
                            supportsNotifications = supportsNotifications,
                        )
                    }
                }

                if (developerSettings != null) {
                    item {
                        Column(modifier = Modifier.padding(8.dp)) {
                            SettingsDialogSectionTitle(text = stringResource(Res.string.developerSettings))
                            Text(
                                "Token: ${developerSettings.token}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Button(onClick = onSendNotifications, enabled = supportsNotifications) {
                                Text("Send Notifications")
                            }
                        }
                    }
                }

                if (developerSettings != null && supportsNotifications) {
                    item {
                        val notificationPermissionState =
                            rememberNotificationPermissionState(userEditableSettings?.useExperimentalFeatures)

                        Column(modifier = Modifier.padding(8.dp)) {
                            Button(
                                onClick = { notificationPermissionState.maybeRequest() },
                            ) {
                                Text("Request Notification Permission")
                            }
                        }
                    }

//                    item {
//                        Column(modifier = Modifier.padding(8.dp)) {
//                            Button(onClick = { controller.openAppSettings() }) {
//                                Text("App Notification Settings")
//                            }
//                        }
//                    }
                }
            }

            HorizontalDivider()

            var developerModeCount by remember { mutableIntStateOf(0) }
            Box(modifier = Modifier.run {
                if (developerSettings == null) {
                    clickable {
                        developerModeCount++
                        if (developerModeCount > 8) {
                            onEnableDeveloperMode()
                        }
                    }
                } else {
                    this
                }
            }) {
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp),
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Keep version so developer mode is accessible
                        Text("Version: ")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPanel(
    settings: UserEditableSettings?,
    supportsNotifications: Boolean,
    onChangeUseExperimentalFeatures: (value: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
    onChangeNotificationsEnabled: (value: Boolean) -> Unit,
) {
    if (settings != null) {
        BooleanSettings(
            title = stringResource(Res.string.enable_notifications),
            value = settings.notificationsEnabled,
            onValueChange = { value -> onChangeNotificationsEnabled(value) },
            enabled = supportsNotifications
        )

        BooleanSettings(
            title = stringResource(Res.string.use_experimental_features),
            value = settings.useExperimentalFeatures,
            onValueChange = { value -> onChangeUseExperimentalFeatures(value) }
        )

        SettingsDialogSectionTitle(text = stringResource(Res.string.dark_mode_preference))
        Column(Modifier.selectableGroup()) {
            SettingsDialogThemeChooserRow(
                text = stringResource(Res.string.dark_mode_config_system_default),
                selected = settings.darkThemeConfig == DarkThemeConfig.FOLLOW_SYSTEM,
                onClick = { onChangeDarkThemeConfig(DarkThemeConfig.FOLLOW_SYSTEM) },
            )
            SettingsDialogThemeChooserRow(
                text = stringResource(Res.string.dark_mode_config_light),
                selected = settings.darkThemeConfig == DarkThemeConfig.LIGHT,
                onClick = { onChangeDarkThemeConfig(DarkThemeConfig.LIGHT) },
            )
            SettingsDialogThemeChooserRow(
                text = stringResource(Res.string.dark_mode_config_dark),
                selected = settings.darkThemeConfig == DarkThemeConfig.DARK,
                onClick = { onChangeDarkThemeConfig(DarkThemeConfig.DARK) },
            )
        }
    }
}

@Composable
private fun BooleanSettings(
    title: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    SettingsDialogSectionTitle(text = title)
    Column(Modifier.selectableGroup()) {
        SettingsDialogThemeChooserRow(
            text = stringResource(Res.string.settings_boolean_true),
            selected = value,
            onClick = { onValueChange(true) },
            enabled = enabled,
        )
        SettingsDialogThemeChooserRow(
            text = stringResource(Res.string.settings_boolean_false),
            selected = !value,
            onClick = { onValueChange(false) },
            enabled = enabled,
        )
    }
}

@Composable
private fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsDialogThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
                enabled = enabled,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}


