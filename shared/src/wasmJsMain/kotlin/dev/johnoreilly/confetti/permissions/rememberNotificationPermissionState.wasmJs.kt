package dev.johnoreilly.confetti.permissions

import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationPermissionState(notificationsActive: Boolean?): NotificationPermissionState {
    return NotificationPermissionState.NotApplicable
}