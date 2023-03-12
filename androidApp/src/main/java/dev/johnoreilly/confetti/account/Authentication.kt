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
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class User(
    val name: String,
    val email: String?,
    val photoUrl: String?,
)

private fun FirebaseUser.toUser(): User {
    return User(
        displayName ?: "",
        email ,
        photoUrl.toString()
    )
}

suspend fun FirebaseUser.idToken(forceRefresh: Boolean): String = suspendCoroutine { continuation ->
    getIdToken(forceRefresh).addOnCompleteListener {
            if (it.isSuccessful) {
                continuation.resume(it.getResult().token!!)
            } else {
                continuation.resume("invalid")
            }
        }
}
class Authentication {
    suspend fun idToken(forceRefresh: Boolean = false): String {
        return Firebase.auth.currentUser?.idToken(forceRefresh) ?: "signedout"
    }


    fun currentUser(): User? {
        return Firebase.auth.currentUser?.toUser()
    }

    fun signOut() {
        Firebase.auth.signOut()
    }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (User) -> Unit,
    onAuthError: (Exception) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            scope.launch {
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                onAuthComplete(authResult.user!!.toUser())
            }
        } catch (e: Exception) {
            onAuthError(e)
        }
    }
}
