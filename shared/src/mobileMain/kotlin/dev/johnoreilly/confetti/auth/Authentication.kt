package dev.johnoreilly.confetti.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.runBlocking

class User(
    val name: String,
    val email: String?,
    val photoUrl: String?,
)

private fun FirebaseUser.toUser(): User {
    return User(
        displayName ?: "",
        email ,
        photoURL.toString()
    )
}

class Authentication {
    suspend fun idToken(forceRefresh: Boolean = false): String? {
        return Firebase.auth.currentUser?.getIdToken(forceRefresh)
    }

    fun currentUser(): User? {
        return Firebase.auth.currentUser?.toUser()
    }

    val currentUserFlow: Flow<User?> = Firebase.auth.authStateChanged.map {it?.toUser() }
        .onStart {
            emit(currentUser())
        }

    /**
     * @throws
     */
    suspend fun signIn(idToken: String): User {
        val credential = GoogleAuthProvider.credential(idToken, null)
        val authResult = Firebase.auth.signInWithCredential(credential)
        return authResult.user!!.toUser()
    }

    fun signOut() {
        // The underlying function is only blocking on JS so it should be OK to block here
        runBlocking {
            Firebase.auth.signOut()
        }
    }
}

