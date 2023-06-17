package dev.johnoreilly.confetti.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking




class DefaultUser(
    override val name: String,
    override val email: String?,
    override val photoUrl: String?,
    override val uid: String,
    private val user_: FirebaseUser?
): User {
    override suspend fun token(forceRefresh: Boolean): String? {
        return try {
            user_?.getIdToken(forceRefresh)
        } catch (e: FirebaseAuthException) {
            // TODO log to firebase
            null
        }
    }
}

class DefaultAuthentication(
    val coroutineScope: CoroutineScope
) : Authentication {
    override val currentUser: StateFlow<User?> = Firebase.auth.authStateChanged.map { it?.toUser() }
        .stateIn(coroutineScope, SharingStarted.Eagerly, Firebase.auth.currentUser?.toUser())

    override suspend fun signIn(idToken: String): SignInResult {
        val credential = GoogleAuthProvider.credential(idToken, null)
        return try {
            Firebase.auth.signInWithCredential(credential)
            SignInSuccess
        } catch (e: FirebaseAuthException) {
            // TODO log to firebase
            SignInError(e)
        } catch (e: Exception) {
            // TODO log serious error
            SignInError(e)
        }
    }

    override fun signOut() {
        // The underlying function is only blocking on JS so it should be OK to block here
        runBlocking {
            Firebase.auth.signOut()
        }
    }
}

private fun FirebaseUser.toUser(): User {
    return DefaultUser(
        name = displayName ?: "",
        email = email,
        photoUrl = photoURL.toString(),
        uid = uid,
        user_ = this
    )
}
