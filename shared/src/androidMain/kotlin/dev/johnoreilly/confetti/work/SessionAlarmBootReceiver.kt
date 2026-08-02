package dev.johnoreilly.confetti.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * BroadcastReceiver triggered when device boots.
 * Reschedule alarms to prevent lost alerts.
 */
class SessionAlarmBootReceiver : BroadcastReceiver(), KoinComponent {

    private val workManager: WorkManager by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("SessionAlarmBootReceiver", "Device booted. Triggering alarm rescheduling.")
            // Reschedule via WorkManager. Prevents ANR on main thread since database
            // query might take longer than boot receiver 10-second limit.
            SessionAlarmRescheduleWorker.enqueue(workManager)
        }
    }
}
