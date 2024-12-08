package dev.johnoreilly.confetti.account

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.johnoreilly.confetti.auth.Authentication

class SignInProcess(
    val credentialManager: CredentialManager,
    val authentication: Authentication,
    val webClientId: String,
) {
    suspend fun signIn(activityContext: Context) {
        val googleIdOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
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

                credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
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

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())

        authentication.signOut()
    }
}
