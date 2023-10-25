package dev.johnoreilly.confetti.account

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auth.Authentication


suspend fun signIn(context: Context, authentication: Authentication) {
    val credentialManager = CredentialManager.create(context)


    val googleIdOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(context.getString(R.string.default_web_client_id))
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(
            context = context,
            request = request
        )

        val credential = result.credential

        when {
            credential is GoogleIdTokenCredential -> {
                authentication.signIn(credential.id)
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
