package dev.johnoreilly.confetti.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import kotlinx.coroutines.launch

@Composable
actual fun rememberNotificationPermissionState(
    notificationsActive: Boolean?,
    onPermissionStatus: (hasPermission: Boolean) -> Unit
): NotificationPermissionState {
    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)
    val coroutineScope = rememberCoroutineScope()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (notificationsActive == true) {
            coroutineScope.launch {
                val permissionState = controller.getPermissionState(Permission.REMOTE_NOTIFICATION)
                if (permissionState != PermissionState.Granted) {
                    onPermissionStatus(false)
                }
            }
        }
    }

    return remember(coroutineScope) {
        NotificationPermissionState.Requestable {
            coroutineScope.launch {
                try {
                    controller.providePermission(Permission.REMOTE_NOTIFICATION)
                    onPermissionStatus(true)
                } catch (e: Exception) {
                    onPermissionStatus(false)
                }
            }
        }
    }
}