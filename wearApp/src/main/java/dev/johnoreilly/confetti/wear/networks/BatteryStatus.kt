package dev.johnoreilly.confetti.wear.networks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_LOW
import android.content.Intent.ACTION_BATTERY_OKAY
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.BatteryManager.ACTION_CHARGING
import android.os.BatteryManager.ACTION_DISCHARGING
import android.os.PowerManager
import android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED
import android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn

class BatteryStatusMonitor(
    private val context: Context,
    coroutineScope: CoroutineScope
) {
    private val powerManager: PowerManager = context.getSystemService(PowerManager::class.java)
    private val batteryManager: BatteryManager = context.getSystemService(BatteryManager::class.java)

    val status: StateFlow<BatteryStatus> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(batteryStatus())
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter().apply {
                addAction(ACTION_DEVICE_IDLE_MODE_CHANGED)
                addAction(ACTION_POWER_SAVE_MODE_CHANGED)
                addAction(ACTION_CHARGING)
                addAction(ACTION_DISCHARGING)
                addAction(ACTION_BATTERY_LOW)
                addAction(ACTION_BATTERY_OKAY)
            },
            ContextCompat.RECEIVER_EXPORTED
        )

        awaitClose { context.unregisterReceiver(receiver) }
    }
        .buffer(capacity = Channel.CONFLATED)
        .stateIn(coroutineScope, started = SharingStarted.Eagerly, initialValue = batteryStatus())

    private fun batteryStatus() =
        BatteryStatus(batteryManager.isCharging, powerManager.isDeviceIdleMode, powerManager.isPowerSaveMode)

    data class BatteryStatus(
        val charging: Boolean,
        val deviceIdleMode: Boolean,
        val powerSaveMode: Boolean
    )
}