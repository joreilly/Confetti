package dev.johnoreilly.confetti.account

import com.google.android.horologist.data.apphelper.AppHelperNodeStatus

data class WearUiState(
    val wearNodes: List<AppHelperNodeStatus> = listOf()
) {
    val showInstallOnWear: Boolean
        get() = wearNodes.isNotEmpty() && wearNodes.find { !it.isAppInstalled } != null
}
