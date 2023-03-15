package dev.johnoreilly.confetti.wear.data.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.horologist.auth.data.ExperimentalHorologistAuthDataApi
import com.google.android.horologist.auth.data.common.model.AuthUser
import com.google.android.horologist.auth.data.common.repository.AuthUserRepository
import com.google.android.horologist.auth.data.googlesignin.AuthUserMapper
import com.google.android.horologist.auth.data.googlesignin.GoogleSignInEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@ExperimentalHorologistAuthDataApi
public class GoogleSignInAuthUserRepository(
    private val applicationContext: Context,
    private val googleSignInClient: GoogleSignInClient
) : AuthUserRepository, GoogleSignInEventListener {
    // simple way to trigger refreshes to the sync GoogleSignIn.getLastSignedInAccount
    private val _authTrigger = MutableStateFlow(0)

    public val authState: Flow<AuthUser?> = _authTrigger.map { getAuthenticated() }

    override suspend fun getAuthenticated(): AuthUser? = withContext(Dispatchers.IO) {
        AuthUserMapper.map(GoogleSignIn.getLastSignedInAccount(applicationContext))
    }

    override suspend fun onSignedIn(account: GoogleSignInAccount) {
        _authTrigger.update { it + 1 }
    }

    public fun onSignedOut() {
        _authTrigger.update { it + 1 }
    }

    public suspend fun signOut() {
        googleSignInClient.signOut().await()
        onSignedOut()
    }
}
