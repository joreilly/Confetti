package dev.johnoreilly.confetti.work

interface NotificationSender {
    suspend fun sendNotification()
}