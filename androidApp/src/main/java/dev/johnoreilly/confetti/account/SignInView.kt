package dev.johnoreilly.confetti.account

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.decompose.SignInComponent
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@Composable
fun SignInRoute(component: SignInComponent) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            var error: String? by remember { mutableStateOf(null) }

            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val authentication = koinInject<Authentication>()
            Button(
                onClick = {
                    scope.launch {
                        signIn(context, authentication)
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.sign_in_google))
            }
            if (error != null) {
                Text(text = error!!, color = Color.Red)
            }
        }
    }
}

suspend fun signIn(context: Context, authentication: Authentication) {
    val credentialManager = CredentialManager.create(context)

    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(context.getString(R.string.default_web_client_id))
        .setAutoSelectEnabled(true)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val result = credentialManager.getCredential(
        context = context,
        request = request
    )

    val credential = result.credential

    if (credential is GoogleIdTokenCredential) {
        authentication.signIn(credential.idToken)
    }
}
