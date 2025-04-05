package dev.johnoreilly.confetti.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import kotlinx.coroutines.launch

@Composable
fun rememberNotificationPermissionState(notificationsActive: Boolean?): NotificationPermissionState {
    return when (notificationsActive) {
        true -> {
            val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
            val controller: PermissionsController = remember(factory) { factory.createPermissionsController() }
            BindEffect(controller)
            val coroutineScope = rememberCoroutineScope()
            return remember(coroutineScope) {
                NotificationPermissionState.Requestable {
                    coroutineScope.launch {
                        val permissionState = controller.getPermissionState(Permission.REMOTE_NOTIFICATION)
                        when (permissionState) {
                            PermissionState.NotDetermined, PermissionState.NotGranted -> {
                                controller.providePermission(Permission.REMOTE_NOTIFICATION)
                            }

                            else -> {}
                        }

                    }
                }
            }
        }
        false -> NotificationPermissionState.NotApplicable
        else -> NotificationPermissionState.NotDetermined
    }
}