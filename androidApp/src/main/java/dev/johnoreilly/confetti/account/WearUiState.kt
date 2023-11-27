package dev.johnoreilly.confetti.account

import com.google.android.horologist.data.apphelper.AppHelperNodeStatus
import com.google.android.horologist.data.apphelper.AppInstallationStatus

data class WearUiState(
    val wearNodes: List<AppHelperNodeStatus> = listOf()
) {
    val showInstallOnWear: Boolean
        get() = wearNodes.find {
            it.appInstallationStatus is AppInstallationStatus.NotInstalled
        } != null
}
