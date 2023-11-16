package dev.johnoreilly.confetti.wear.networks

import com.google.android.horologist.networks.data.NetworkInfo
import com.google.android.horologist.networks.data.NetworkStatus
import com.google.android.horologist.networks.data.Networks
import com.google.android.horologist.networks.data.RequestType
import com.google.android.horologist.networks.rules.Allow
import com.google.android.horologist.networks.rules.NetworkingRules
import com.google.android.horologist.networks.rules.RequestCheck

object WearNetworkingRules: NetworkingRules {

    override fun isHighBandwidthRequest(requestType: RequestType): Boolean {
        return false
    }

    override fun checkValidRequest(
        requestType: RequestType,
        currentNetworkInfo: NetworkInfo,
    ): RequestCheck {
        return Allow
    }

    override fun getPreferredNetwork(
        networks: Networks,
        requestType: RequestType,
    ): NetworkStatus? {
        val wifi = networks.networks.firstOrNull { it.networkInfo is NetworkInfo.Wifi }
        return wifi ?: networks.networks.firstOrNull()
    }
}