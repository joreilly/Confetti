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
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.SignInSuccess
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.auth.toUser
import dev.johnoreilly.confetti.wear.WearSettingsSync
import kotlinx.coroutines.launch


@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: () -> Unit,
    onAuthError: (Exception) -> Unit,
    authentication: Authentication,
    wearSettingsSync: WearSettingsSync
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val idToken = task.getResult(ApiException::class.java)!!.idToken!!
            scope.launch {
                val result = authentication.signIn(idToken)
                if (result is SignInSuccess) {
                    wearSettingsSync.updateAuthToken(result.user.toUser())
                } else {
                    wearSettingsSync.updateAuthToken(null)
                }
                onAuthComplete()
            }
        } catch (e: Exception) {
            onAuthError(e)
        }
    }
}

