package dev.johnoreilly.confetti.account

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.johnoreilly.confetti.auth.Authentication

class SignInProcess(
    val credentialManager: CredentialManager,
    val authentication: Authentication,
    val webClientId: String,
    val useSignInWithGoogle: Boolean = true,
) {
    /**
     * Initiates the sign-in process.
     *
     * This function attempts to sign in the user using Sign in With Google.
     * It first checks if `useSignInWithGoogle` is enabled and builds the appropriate credential option.
     * Then, it sends a request to the Credential Manager to obtain a credential.
     * If successful, it extracts the ID token from the credential and passes it to [Authentication].
     * In case of errors, it handles exceptions and logs them.
     *
     * @see https://developer.android.com/identity/sign-in/credential-manager-siwg
     *
     * @param activityContext The context of the activity initiating the sign-in process.
     */
    suspend fun signIn(activityContext: Context) {
        val googleAuthOption = if (useSignInWithGoogle) {
            GetSignInWithGoogleOption.Builder(webClientId)
                .build()
        } else {
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .build()
        }

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleAuthOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential

            when {
                credential is GoogleIdTokenCredential -> {
                    authentication.signIn(credential.idToken)
                }

                credential is CustomCredential -> {
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)
                    authentication.signIn(googleIdTokenCredential.idToken)
                }

                else -> {
                    println("Unknown auth $credential")
                }
            }
        } catch (e: NoCredentialException) {
            println("NoCredentialException")
            // TODO show some failure
        } catch (e: GetCredentialCancellationException) {
            // ignored
        } catch (e: GetCredentialException) {
            println("Auth failed $e")
            // TODO show some failure
        }
    }

    /**
     * Signs the user out of the application.
     *
     * This function performs the following actions:
     * 1. Clears the stored user credentials using [CredentialManager].
     * 2. Signs the user out via [Authentication].
     */
    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())

        authentication.signOut()
    }
}
