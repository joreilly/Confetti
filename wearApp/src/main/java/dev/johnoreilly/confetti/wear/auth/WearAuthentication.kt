package dev.johnoreilly.confetti.wear.auth

import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * Authentication implementation for Wear OS that leverages phone authentication.
 *
 * This class primarily uses a default local authentication mechanism but adds the ability to
 * seamlessly sign in using an ID token synced from the user's phone.
 *
 * @param settingsSync An instance of [PhoneSettingsSync] to access phone settings,
 * specifically the ID token.
 * @param defaultAuthentication The default authentication mechanism to be used when
 * phone authentication is not available or fails.
 * @param coroutineScope The coroutine scope in which the authentication operations are performed.
 */
class WearAuthentication(
    val settingsSync: PhoneSettingsSync,
    val defaultAuthentication: Authentication,
    val coroutineScope: CoroutineScope,
) : Authentication by defaultAuthentication {
    override val currentUser: StateFlow<User?> = flow {
        val localUser = defaultAuthentication.currentUser.value
        val mobileToken = settingsSync.settingsFlow.first().idToken

        if (localUser == null && mobileToken != null) {
            defaultAuthentication.signIn(mobileToken)
        }

        emitAll(defaultAuthentication.currentUser)
    }.stateIn(
        coroutineScope,
        started = SharingStarted.Lazily,
        initialValue = defaultAuthentication.currentUser.value
    )
}