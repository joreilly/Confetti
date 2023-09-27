package dev.johnoreilly.confetti.account

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auth.Authentication


suspend fun signIn(context: Context, authentication: Authentication) {
    val credentialManager = CredentialManager.create(context)

    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.getString(R.string.default_web_client_id))
        .setAutoSelectEnabled(true)
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

        if (credential is GoogleIdTokenCredential) {
            authentication.signIn(credential.idToken)
        } else {
            println("Unknown auth $credential")
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
