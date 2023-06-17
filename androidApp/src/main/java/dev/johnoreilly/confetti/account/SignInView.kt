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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.decompose.SignInComponent
import org.koin.compose.koinInject


@Composable
fun SignInRoute(component: SignInComponent) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            var error: String? by remember { mutableStateOf(null) }
            val launcher = rememberFirebaseAuthLauncher(
                onAuthComplete = component::onCloseClicked,
                onAuthError = {
                    it.printStackTrace()
                    error = "Something went wrong"
                },
                koinInject()
            )

            val context = LocalContext.current
            Button(
                onClick = {
                    launcher.launch(googleSignInClient(context).signInIntent)
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


fun googleSignInClient(context: Context) = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestIdToken(context.getString(R.string.default_web_client_id))
    .requestEmail()
    .build().let { GoogleSignIn.getClient(context, it) }
