package dev.johnoreilly.confetti.permissions

import androidx.compose.runtime.Composable

@Composable
expect fun rememberNotificationPermissionState(
    notificationsActive: Boolean?,
    onPermissionStatus: (hasPermission: Boolean) -> Unit = {}
): NotificationPermissionState