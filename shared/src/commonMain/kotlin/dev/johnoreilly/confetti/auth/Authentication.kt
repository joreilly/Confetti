package dev.johnoreilly.confetti.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


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

    object Disabled: Authentication {
        override val currentUser: StateFlow<User?> = MutableStateFlow(null)

        override suspend fun signIn(idToken: String): SignInResult =
            SignInError(Exception("Firebase is not available"))

        override fun signOut() {
        }
    }
}

sealed interface SignInResult
data object SignInSuccess : SignInResult
class SignInError(val e: Exception) : SignInResult