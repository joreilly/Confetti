package dev.johnoreilly.confetti.wear.networks

import com.google.android.horologist.networks.data.NetworkInfo
import com.google.android.horologist.networks.data.NetworkStatus
import com.google.android.horologist.networks.data.NetworkType
import com.google.android.horologist.networks.data.Networks
import com.google.android.horologist.networks.data.RequestType
import com.google.android.horologist.networks.rules.Allow
import com.google.android.horologist.networks.rules.Fail
import com.google.android.horologist.networks.rules.NetworkingRules
import com.google.android.horologist.networks.rules.RequestCheck
import dev.johnoreilly.confetti.wear.proto.NetworkPreferences
import dev.johnoreilly.confetti.wear.proto.WearSettings
import dev.johnoreilly.confetti.wear.settings.WearPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class WearNetworkingRules(
    val batteryStatusMonitor: BatteryStatusMonitor,
    val wearPreferences: WearPreferencesStore
) : NetworkingRules {

    override fun isHighBandwidthRequest(requestType: RequestType): Boolean {
        val preferences = wearPreferences.networkPreferences

        if (preferences.preferWifi) {
            return requestType != RequestType.LogsRequest
        }

        return false
    }

    override fun checkValidRequest(
        requestType: RequestType,
        currentNetworkInfo: NetworkInfo,
    ): RequestCheck {
        val preferences = wearPreferences.networkPreferences
        val battery = batteryStatusMonitor.status.value

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
        val battery = batteryStatusMonitor.status.value

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

        return if (battery.charging || preferences.preferWifi) {
            wifi ?: bt ?: cell
        } else {
            bt ?: wifi ?: cell
        }
    }
}