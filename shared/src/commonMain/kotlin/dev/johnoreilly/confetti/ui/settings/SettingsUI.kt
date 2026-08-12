@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import dev.johnoreilly.confetti.ui.component.ConfettiAlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.cancel
import confetti.shared.generated.resources.dark_mode_config_dark
import confetti.shared.generated.resources.dark_mode_config_light
import confetti.shared.generated.resources.dark_mode_config_system_default
import confetti.shared.generated.resources.dark_mode_preference
import confetti.shared.generated.resources.developerSettings
import confetti.shared.generated.resources.enable_notifications
import confetti.shared.generated.resources.enable_notifications_desc
import confetti.shared.generated.resources.settings_boolean_false
import confetti.shared.generated.resources.settings_boolean_true
import confetti.shared.generated.resources.settings_title
import confetti.shared.generated.resources.report_issue
import confetti.shared.generated.resources.report_issue_desc
import confetti.shared.generated.resources.use_experimental_features
import confetti.shared.generated.resources.use_experimental_features_desc
import dev.johnoreilly.confetti.appconfig.ApplicationInfo
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
        applicationInfo = component.applicationInfo,
        onEnableDeveloperMode = component::enableDeveloperMode,
        onSendNotifications = component::sendNotifications,
        supportsNotifications = component.supportsNotifications,
        onNotificationsEnabled = component::updateNotificationsEnabled,
        onChangeForceEnableAssistant = component::updateForceEnableAssistant,
        popBack = popBack
    )
}

@Composable
fun SettingsUI(
    userEditableSettings: UserEditableSettings?,
    onChangeUseExperimentalFeatures: (value: Boolean) -> Unit,
    onChangeDarkThemeConfig: (darkThemeConfig: DarkThemeConfig) -> Unit,
    developerSettings: DeveloperSettings?,
    applicationInfo: ApplicationInfo,
    onEnableDeveloperMode: () -> Unit,
    onSendNotifications: () -> Unit,
    supportsNotifications: Boolean,
    onNotificationsEnabled: (value: Boolean) -> Unit,
    onChangeForceEnableAssistant: (value: Boolean) -> Unit,
    popBack: () -> Unit
) {
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    val notificationPermissionState = rememberNotificationPermissionState(
        notificationsActive = userEditableSettings?.notificationsEnabled,
        onPermissionDeniedAlways = {
            showPermissionDeniedDialog = true
        },
        onPermissionStatus = { hasPermission ->
            if (userEditableSettings?.notificationsEnabled != hasPermission) {
                onNotificationsEnabled(hasPermission)
            }
        }
    )

    if (showPermissionDeniedDialog) {
        ConfettiAlertDialog(
            title = "Permission Required",
            text = "Notification permission was permanently denied. Please enable it in Settings to receive updates.",
            confirmText = "Open Settings",
            onConfirm = {
                showPermissionDeniedDialog = false
                notificationPermissionState.openSettings()
            },
            dismissText = "Cancel",
            onDismiss = { showPermissionDeniedDialog = false }
        )
    }

    val scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uriHandler = LocalUriHandler.current
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
                    SettingsPanel(
                        settings = userEditableSettings,
                        onChangeDarkThemeConfig = onChangeDarkThemeConfig,
                        onChangeUseExperimentalFeatures = onChangeUseExperimentalFeatures,
                        onChangeNotificationsEnabled = { enabled ->
                                if (enabled) {
                                    notificationPermissionState.maybeRequest()
                                } else {
                                    onNotificationsEnabled(false)
                                }
                            },
                        supportsNotifications = supportsNotifications,
                    )
                }

                item {
                    ActionSettingsRow(
                        title = stringResource(Res.string.report_issue),
                        subtitle = stringResource(Res.string.report_issue_desc),
                        onClick = { uriHandler.openUri("https://github.com/joreilly/Confetti/issues/new") }
                    )
                }

                if (developerSettings != null) {
                    item {
                        Text(
                            text = stringResource(Res.string.developerSettings),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                        )
                    }

                    developerSettings.token?.let { token ->
                        item {
                            TokenSettingsRow(token = token)
                        }
                    }

                    item {
                        ActionSettingsRow(
                            title = "Send Test Notification",
                            subtitle = "Trigger a mock session reminder alert",
                            onClick = onSendNotifications,
                            enabled = supportsNotifications
                        )
                    }

                    item {
                        SwitchSettingsRow(
                            title = "Force Open Assistant",
                            description = "Enable Gemini Assistant without API key config",
                            value = developerSettings.forceEnableAssistant,
                            onValueChange = onChangeForceEnableAssistant
                        )
                    }
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
                        Text("Version: ${applicationInfo.versionName}")
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
        var showThemeDialog by remember { mutableStateOf(false) }

        SwitchSettingsRow(
            title = stringResource(Res.string.enable_notifications),
            description = stringResource(Res.string.enable_notifications_desc),
            value = settings.notificationsEnabled,
            onValueChange = onChangeNotificationsEnabled,
            enabled = supportsNotifications
        )

        SwitchSettingsRow(
            title = stringResource(Res.string.use_experimental_features),
            description = stringResource(Res.string.use_experimental_features_desc),
            value = settings.useExperimentalFeatures,
            onValueChange = onChangeUseExperimentalFeatures
        )

        val themeLabel = when (settings.darkThemeConfig) {
            DarkThemeConfig.FOLLOW_SYSTEM -> stringResource(Res.string.dark_mode_config_system_default)
            DarkThemeConfig.LIGHT -> stringResource(Res.string.dark_mode_config_light)
            DarkThemeConfig.DARK -> stringResource(Res.string.dark_mode_config_dark)
        }

        SelectionSettingsRow(
            title = stringResource(Res.string.dark_mode_preference),
            selectedValue = themeLabel,
            onClick = { showThemeDialog = true }
        )

        if (showThemeDialog) {
            DarkThemeConfigDialog(
                currentConfig = settings.darkThemeConfig,
                onConfigSelected = onChangeDarkThemeConfig,
                onDismissRequest = { showThemeDialog = false }
            )
        }
    }
}

@Composable
private fun SwitchSettingsRow(
    title: String,
    description: String? = null,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Switch) { onValueChange(!value) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
        }
        Switch(
            checked = value,
            onCheckedChange = null,
            enabled = enabled
        )
    }
}

@Composable
private fun SelectionSettingsRow(
    title: String,
    selectedValue: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = selectedValue,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        }
    }
}

@Composable
private fun DarkThemeConfigDialog(
    currentConfig: DarkThemeConfig,
    onConfigSelected: (DarkThemeConfig) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(Res.string.dark_mode_preference),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(Modifier.selectableGroup()) {
                SettingsDialogThemeChooserRow(
                    text = stringResource(Res.string.dark_mode_config_system_default),
                    selected = currentConfig == DarkThemeConfig.FOLLOW_SYSTEM,
                    onClick = {
                        onConfigSelected(DarkThemeConfig.FOLLOW_SYSTEM)
                        onDismissRequest()
                    }
                )
                SettingsDialogThemeChooserRow(
                    text = stringResource(Res.string.dark_mode_config_light),
                    selected = currentConfig == DarkThemeConfig.LIGHT,
                    onClick = {
                        onConfigSelected(DarkThemeConfig.LIGHT)
                        onDismissRequest()
                    }
                )
                SettingsDialogThemeChooserRow(
                    text = stringResource(Res.string.dark_mode_config_dark),
                    selected = currentConfig == DarkThemeConfig.DARK,
                    onClick = {
                        onConfigSelected(DarkThemeConfig.DARK)
                        onDismissRequest()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
        }
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

@Composable
private fun TokenSettingsRow(
    token: String
) {
    val clipboardManager = LocalClipboardManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                clipboardManager.setText(AnnotatedString(token))
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Developer Token",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = token,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActionSettingsRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
        }
    }
}


