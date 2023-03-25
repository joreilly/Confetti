@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.data.auth

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.auth.data.common.model.AuthUser
import com.google.android.horologist.auth.data.common.repository.AuthUserRepository
import com.google.android.horologist.auth.data.googlesignin.GoogleSignInEventListener
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseAuthUserRepository : AuthUserRepository, GoogleSignInEventListener {
    val auth = Firebase.auth

    val authState: Flow<AuthUser?> = callbackFlow {
        trySendBlocking(auth.currentUser)
        val listener = AuthStateListener {
            trySendBlocking(it.currentUser)
        }
        auth.addAuthStateListener(listener)

        awaitClose { auth.removeAuthStateListener(listener) }
    }.map { FirebaseUserMapper.map(it) }

    override suspend fun onSignedIn(account: GoogleSignInAccount) {
        val idToken = account.idToken
        val credential = GoogleAuthProvider.getCredential(idToken!!, null)
        Firebase.auth.signInWithCredential(credential).await()
    }

    override suspend fun getAuthenticated(): AuthUser? =
        FirebaseUserMapper.map(auth.currentUser)
}
