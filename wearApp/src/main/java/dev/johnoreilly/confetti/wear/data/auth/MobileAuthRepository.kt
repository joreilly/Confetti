@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.data.auth

import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.auth.data.common.model.AuthUser
import com.google.android.horologist.auth.data.common.repository.AuthUserRepository
import dev.johnoreilly.confetti.wear.proto.UserAuth
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

public class MobileAuthRepository(
    private val phoneSettingsSync: PhoneSettingsSync,
    private val googleSignInRepository: GoogleSignInAuthUserRepository,
    private val coroutineScope: CoroutineScope
) : AuthUserRepository {
    val authState = combine(
        phoneSettingsSync.settingsFlow,
        googleSignInRepository.authState
    ) { settings, localAuth ->
        val mobileAuth = settings.userAuth?.toAuthUser()

        if (mobileAuth != null) {
            AuthAndSource(mobileAuth, AuthSource.Mobile)
        } else if (localAuth != null) {
            AuthAndSource(localAuth, AuthSource.GoogleSignIn)
        } else {
            null
        }
    }

    override suspend fun getAuthenticated(): AuthUser? = authState.first()?.user
}

private fun UserAuth.toAuthUser(): AuthUser =
    AuthUser(displayName = this.name, email = this.email, avatarUri = this.photoUrl)
