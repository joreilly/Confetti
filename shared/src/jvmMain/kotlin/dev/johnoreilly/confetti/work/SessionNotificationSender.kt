package dev.johnoreilly.confetti.dev.johnoreilly.confetti.work

import dev.johnoreilly.confetti.work.NotificationSender

class SessionNotificationSender: NotificationSender {
    override suspend fun sendNotification(selector: NotificationSender.Selector) {
    }
}