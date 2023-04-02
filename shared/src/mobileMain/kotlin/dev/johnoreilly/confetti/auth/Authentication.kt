package dev.johnoreilly.confetti.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import dev.johnoreilly.confetti.TokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

interface User: TokenProvider {
    val name: String
    val email: String?
    val photoUrl: String?
    val uid: String
}

interface Authentication {
    /**
     * A state flow that returns the current user
     */
    val currentUser: StateFlow<User?>

    /**
     * Sign in to firebase. A successful call returns [SignInSuccess] and triggers the emission
     * of a new [currentUser]
     */
    suspend fun signIn(idToken: String): SignInResult

    /**
     * Sign out of firebase. triggers the emission of a new [currentUser]
     */
    fun signOut()
}

sealed interface SignInResult
data class SignInSuccess(val user: FirebaseUser) : SignInResult
data class SignInError(val e: Exception) : SignInResult

class DefaultUser(
    override val name: String,
    override val email: String?,
    override val photoUrl: String?,
    override val uid: String,
    private val user_: FirebaseUser?
): User {
    override suspend fun token(forceRefresh: Boolean): String? {
        return user_?.getIdToken(forceRefresh)
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
            val auth = Firebase.auth.signInWithCredential(credential)
            SignInSuccess(auth.user!!)
        } catch (e: Exception) {
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

fun FirebaseUser.toUser(): User {
    return DefaultUser(
        name = displayName ?: "",
        email = email,
        photoUrl = photoURL.toString(),
        uid = uid,
        user_ = this
    )
}
