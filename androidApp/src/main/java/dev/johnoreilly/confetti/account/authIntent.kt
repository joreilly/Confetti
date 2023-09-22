package dev.johnoreilly.confetti.account

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import kotlinx.coroutines.launch


@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: () -> Unit,
    onAuthError: (Exception) -> Unit,
    authentication: Authentication,
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val idToken = task.getResult(ApiException::class.java)!!.idToken!!
            scope.launch {
                authentication.signIn(idToken)
                onAuthComplete()
            }
        } catch (e: Exception) {
            onAuthError(e)
        }
    }
}

