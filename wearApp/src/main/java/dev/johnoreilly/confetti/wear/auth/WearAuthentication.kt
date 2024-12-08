package dev.johnoreilly.confetti.wear.auth

import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.SignInError
import dev.johnoreilly.confetti.auth.SignInResult
import dev.johnoreilly.confetti.auth.SignInSuccess
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

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

    override suspend fun signIn(idToken: String): SignInResult {
        val token = settingsSync.settingsFlow.first().idToken

        return if (token != null) {
            defaultAuthentication.signIn(token)
            SignInSuccess
        } else {
            defaultAuthentication.signOut()
            SignInError(Exception("No mobile idToken"))
        }
    }

    override fun signOut() {
        defaultAuthentication.signOut()
    }
}