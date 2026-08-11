package dev.johnoreilly.confetti.permissions

sealed interface NotificationPermissionState {
    fun maybeRequest() {}
    fun openSettings() {}

    data object NotApplicable: NotificationPermissionState

    object NotDetermined: NotificationPermissionState

    class Requestable(
        private val onRequest: () -> Unit,
        private val onOpenSettings: () -> Unit = {}
    ): NotificationPermissionState {
        override fun maybeRequest() {
            onRequest()
        }

        override fun openSettings() {
            onOpenSettings()
        }
    }
}