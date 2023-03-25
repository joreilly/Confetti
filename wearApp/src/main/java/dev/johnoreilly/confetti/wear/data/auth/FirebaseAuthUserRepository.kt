@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.data.auth

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.auth.data.common.model.AuthUser
import com.google.android.horologist.auth.data.common.repository.AuthUserRepository
import com.google.android.horologist.auth.data.googlesignin.GoogleSignInEventListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseAuthUserRepository(
    val phoneSettingsSync: PhoneSettingsSync,
    val auth: FirebaseAuth,
    val googleSignIn: GoogleSignInClient
) : AuthUserRepository, GoogleSignInEventListener {
    val localAuthState: Flow<AuthUser?> = callbackFlow {
        val currentUser = auth.currentUser

        trySendBlocking(currentUser)
        val listener = AuthStateListener {
            trySendBlocking(it.currentUser)
        }
        auth.addAuthStateListener(listener)

        awaitClose { auth.removeAuthStateListener(listener) }
    }.map { FirebaseUserMapper.map(it) }

    val remoteAuth = phoneSettingsSync.settingsFlow.map {
        it.authToken
    }

    override suspend fun onSignedIn(account: GoogleSignInAccount) {
        val idToken = account.idToken
        try {
            // TODO how to do this earlier?
            // com.google.firebase.auth.FirebaseAuthInvalidCredentialsException:
            // The supplied auth credential is malformed or has expired.
            val credential = GoogleAuthProvider.getCredential(idToken!!, null)
            Firebase.auth.signInWithCredential(credential).await()
        } catch (fae: FirebaseAuthException) {
            Log.w("FirebaseAuthUserRepository", fae)
            googleSignIn.signOut().await()
        }
    }

    override suspend fun getAuthenticated(): AuthUser? =
        FirebaseUserMapper.map(auth.currentUser)
}
