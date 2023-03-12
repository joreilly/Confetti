package dev.johnoreilly.confetti.work

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.WorkManager
import dev.johnoreilly.confetti.shared.R
import java.util.UUID

fun createNotification(
    context: Context,
    workRequestId: UUID
): Notification = NotificationCompat.Builder(context, RefreshWorker.WorkChannelId)
    .setContentTitle(context.getString(R.string.work_title))
    .setTicker(context.getString(R.string.work_title))
    .setSmallIcon(R.drawable.baseline_refresh_24)
    .addAction(
        android.R.drawable.ic_delete,
        context.getString(R.string.work_cancel_text),
        WorkManager.getInstance(context).createCancelPendingIntent(workRequestId)
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                context,
                RefreshWorker.WorkChannelId,
                context.getString(R.string.work_channel_name)
            ).also {
                setChannelId(it.id)
            }
        }
    }.build()

@TargetApi(Build.VERSION_CODES.O)
fun createNotificationChannel(
    context: Context,
    channelId: String,
    name: String,
    notificationImportance: Int = NotificationManager.IMPORTANCE_HIGH
): NotificationChannel {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return NotificationChannel(
        channelId, name, notificationImportance
    ).also { channel ->
        notificationManager.createNotificationChannel(channel)
    }
}
