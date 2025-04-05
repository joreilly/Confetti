package dev.johnoreilly.confetti.permissions

sealed interface NotificationPermissionState {
    fun maybeRequest() {}

    data object NotApplicable: NotificationPermissionState

    object NotDetermined: NotificationPermissionState

    class Requestable(private val onRequest: () -> Unit): NotificationPermissionState {
        override fun maybeRequest() {
            onRequest()
        }
    }
}