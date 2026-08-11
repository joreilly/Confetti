package dev.johnoreilly.confetti.permissions

import androidx.compose.runtime.Composable

@Composable
expect fun rememberNotificationPermissionState(
    notificationsActive: Boolean?,
    onPermissionDeniedAlways: () -> Unit = {},
    onPermissionStatus: (hasPermission: Boolean) -> Unit = {}
): NotificationPermissionState