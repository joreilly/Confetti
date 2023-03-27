package dev.johnoreilly.confetti.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dev.johnoreilly.confetti.R
import org.koin.compose.koinInject


@Composable
fun SignInRoute(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        var error: String? by remember { mutableStateOf(null) }
        val launcher = rememberFirebaseAuthLauncher(
            onAuthComplete = { _ ->
                onBackClick()
            },
            onAuthError = {
                it.printStackTrace()
                error = "Something went wrong"
            },
            koinInject()
        )

        val context = LocalContext.current
        Button(
            modifier = Modifier.align(Alignment.Center),
            onClick = {
                val gso =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
            }
        ) {
            Text(text = "Sign in with Google")
        }
    }
}
