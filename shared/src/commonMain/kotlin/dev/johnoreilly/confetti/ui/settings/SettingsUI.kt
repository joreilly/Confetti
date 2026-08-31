@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import confetti.shared.generated.resources.slack_title
import confetti.shared.generated.resources.slack_desc
import confetti.shared.generated.resources.use_experimental_features
import confetti.shared.generated.resources.use_experimental_features_desc
import dev.johnoreilly.confetti.appconfig.ApplicationInfo
import dev.johnoreilly.confetti.decompose.DarkThemeConfig
import dev.johnoreilly.confetti.decompose.DeveloperSettings
import dev.johnoreilly.confetti.decompose.SettingsComponent
import dev.johnoreilly.confetti.decompose.UserEditableSettings
import dev.johnoreilly.confetti.permissions.rememberNotificationPermissionState
import dev.johnoreilly.confetti.preview.MobilePreviews
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
        onUpdateDeveloperMode = component::updateDeveloperMode,
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
    onUpdateDeveloperMode: (value: Boolean) -> Unit,
    onSendNotifications: () -> Unit,
    supportsNotifications: Boolean,
    onNotificationsEnabled: (value: Boolean) -> Unit,
    onChangeForceEnableAssistant: (value: Boolean) -> Unit,
    popBack: () -> Unit
) {
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showNotificationInfoDialog by remember { mutableStateOf(false) }
    var showTokenInfoDialog by remember { mutableStateOf(false) }

    if (showNotificationInfoDialog) {
        ConfettiAlertDialog(
            title = "Mock Notifications",
            text = "Triggers local notifications for all bookmarked sessions (past and upcoming) in your selected conference to test notification alerts.",
            confirmText = "Got it",
            onConfirm = { showNotificationInfoDialog = false },
            onDismiss = { showNotificationInfoDialog = false }
        )
    }

    if (showTokenInfoDialog) {
        ConfettiAlertDialog(
            title = "Developer Token",
            text = "An authentication token used for API requests and debugging. Tapping the row copies the token to your clipboard when signed in.",
            confirmText = "Got it",
            onConfirm = { showTokenInfoDialog = false },
            onDismiss = { showTokenInfoDialog = false }
        )
    }

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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Column(
            Modifier
                .clipToBounds()
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Group 1: Preferences (Theme & Notifications)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            if (userEditableSettings != null) {
                                SwitchSettingsRow(
                                    title = stringResource(Res.string.enable_notifications),
                                    description = stringResource(Res.string.enable_notifications_desc),
                                    value = userEditableSettings.notificationsEnabled,
                                    onValueChange = { enabled ->
                                        if (enabled) {
                                            notificationPermissionState.maybeRequest()
                                        } else {
                                            onNotificationsEnabled(false)
                                        }
                                    },
                                    enabled = supportsNotifications
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )

                                var showThemeDialog by remember { mutableStateOf(false) }
                                val themeLabel = when (userEditableSettings.darkThemeConfig) {
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
                                        currentConfig = userEditableSettings.darkThemeConfig,
                                        onConfigSelected = onChangeDarkThemeConfig,
                                        onDismissRequest = { showThemeDialog = false }
                                    )
                                }
                            }
                        }
                    }
                }

                // Group 2: Developer Options
                item {
                    val isDevMode = developerSettings != null

                    val bannerContainerColor by animateColorAsState(
                        targetValue = if (isDevMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        label = "developerOptionsBannerContainerColor"
                    )
                    val bannerContentColor by animateColorAsState(
                        targetValue = if (isDevMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        label = "developerOptionsBannerContentColor"
                    )

                    // Master Banner Card (Android System Settings Style)
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = bannerContainerColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateDeveloperMode(!isDevMode) }
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Use developer options",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = bannerContentColor
                            )
                            Switch(
                                checked = isDevMode,
                                onCheckedChange = null
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isDevMode,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    if (developerSettings != null) {
                                        TokenSettingsRow(
                                            token = developerSettings.token,
                                            onInfoClick = { showTokenInfoDialog = true }
                                        )

                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        )

                                        ActionSettingsRow(
                                            title = "Send Test Notification",
                                            subtitle = "Trigger a mock session reminder alert",
                                            onClick = onSendNotifications,
                                            onInfoClick = { showNotificationInfoDialog = true },
                                            enabled = supportsNotifications
                                        )

                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        )

                                        SwitchSettingsRow(
                                            title = "Force Open Assistant",
                                            description = "Enable Gemini Assistant without API key config",
                                            value = developerSettings.forceEnableAssistant,
                                            onValueChange = onChangeForceEnableAssistant
                                        )
                                    }

                                    if (userEditableSettings != null) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                        )

                                        SwitchSettingsRow(
                                            title = stringResource(Res.string.use_experimental_features),
                                            description = stringResource(Res.string.use_experimental_features_desc),
                                            value = userEditableSettings.useExperimentalFeatures,
                                            onValueChange = onChangeUseExperimentalFeatures
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Group 3: Version info footer
                item {
                    var developerModeCount by remember { mutableIntStateOf(0) }
                    val clipboardManager = LocalClipboardManager.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = navigationBarsPadding + 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Confetti v${applicationInfo.versionName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(applicationInfo.versionName))
                                    if (developerSettings == null) {
                                        developerModeCount++
                                        if (developerModeCount > 8) {
                                            onEnableDeveloperMode()
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
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
    token: String?,
    onInfoClick: (() -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val hasToken = !token.isNullOrBlank()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasToken) {
                if (token != null) {
                    clipboardManager.setText(AnnotatedString(token))
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Developer Token",
                style = MaterialTheme.typography.bodyLarge,
                color = if (hasToken) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (hasToken) token.orEmpty() else "Not signed in",
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasToken) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onInfoClick != null) {
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionSettingsRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    onInfoClick: (() -> Unit)? = null,
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
        if (onInfoClick != null) {
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@MobilePreviews
@Composable
private fun SettingsScreenPreview() {
    SettingsPreviewContent()
}

/** Stable sample state used by platform preview catalogs for the redesigned settings screen. */
@Composable
fun SettingsPreviewContent() {
    SettingsUI(
        userEditableSettings = UserEditableSettings(
            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            useExperimentalFeatures = true,
            notificationsEnabled = true
        ),
        onChangeUseExperimentalFeatures = {},
        onChangeDarkThemeConfig = {},
        developerSettings = DeveloperSettings(
            token = "sample-token-12345",
            forceEnableAssistant = false
        ),
        applicationInfo = ApplicationInfo("1.0.0 (100)"),
        onEnableDeveloperMode = {},
        onUpdateDeveloperMode = {},
        onSendNotifications = {},
        supportsNotifications = true,
        onNotificationsEnabled = {},
        onChangeForceEnableAssistant = {},
        popBack = {}
    )
}

