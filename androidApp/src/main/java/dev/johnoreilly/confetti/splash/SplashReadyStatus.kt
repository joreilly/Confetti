package dev.johnoreilly.confetti.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * A container of if the app is ready to get rid of the Splash screen or not
 * Can be used by top level destinations like the conferences list of the sessions to report when
 * they're done loading.
 */
class SplashReadyStatus {
    var isAppReadyToShow by mutableStateOf(false)
        private set

    fun reportIsReady() {
        isAppReadyToShow = true
    }
}