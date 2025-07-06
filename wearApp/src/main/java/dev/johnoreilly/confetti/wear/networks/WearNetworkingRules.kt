@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.networks

import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.networks.battery.BatteryStatusMonitor
import com.google.android.horologist.networks.data.NetworkInfo
import com.google.android.horologist.networks.data.NetworkStatus
import com.google.android.horologist.networks.data.NetworkType
import com.google.android.horologist.networks.data.Networks
import com.google.android.horologist.networks.data.RequestType
import com.google.android.horologist.networks.rules.Allow
import com.google.android.horologist.networks.rules.Fail
import com.google.android.horologist.networks.rules.NetworkingRules
import com.google.android.horologist.networks.rules.RequestCheck
import dev.johnoreilly.confetti.wear.settings.WearPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class WearNetworkingRules(
    batteryStatusMonitor: BatteryStatusMonitor,
    val wearPreferences: WearPreferencesStore,
    coroutineScope: CoroutineScope
) : NetworkingRules {
    val battery = batteryStatusMonitor.status.stateIn(
        coroutineScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BatteryStatusMonitor.BatteryStatus(
            charging = false,
            deviceIdleMode = false,
            powerSaveMode = false
        )
    )

    override fun isHighBandwidthRequest(requestType: RequestType): Boolean {
        return false
    }

    override fun checkValidRequest(
        requestType: RequestType,
        currentNetworkInfo: NetworkInfo,
    ): RequestCheck {
        val preferences = wearPreferences.networkPreferences
        val battery = battery.value

        if (requestType == RequestType.LogsRequest && !battery.charging) {
            return Fail("Log requests only while charging")
        }

        if (currentNetworkInfo.type == NetworkType.Cell && !preferences.allowLte) {
            return Fail("LTE not allowed")
        }

        return Allow
    }

    override fun getPreferredNetwork(
        networks: Networks,
        requestType: RequestType,
    ): NetworkStatus? {
        val preferences = wearPreferences.networkPreferences
        val battery = battery.value

        if (requestType == RequestType.LogsRequest && !battery.charging) {
            return null
        }

        val bt = networks.networks.firstOrNull { it.networkInfo is NetworkInfo.Bluetooth }
        val wifi = networks.networks.firstOrNull { it.networkInfo is NetworkInfo.Wifi }
        val cell = if (preferences.allowLte) {
            networks.networks.firstOrNull { it.networkInfo is NetworkInfo.Cellular }
        } else {
            null
        }

        return bt ?: wifi ?: cell
    }
}