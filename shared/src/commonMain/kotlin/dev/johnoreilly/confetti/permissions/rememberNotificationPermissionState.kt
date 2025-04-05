package dev.johnoreilly.confetti.permissions

import androidx.compose.runtime.Composable

@Composable
expect fun rememberNotificationPermissionState(notificationsActive: Boolean?): NotificationPermissionState