package dev.johnoreilly.confetti.permissions

import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationPermissionState(
    notificationsActive: Boolean?,
    onPermissionStatus: (hasPermission: Boolean) -> Unit
): NotificationPermissionState {
    return NotificationPermissionState.NotApplicable
}