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
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
        photoURL.toString()
    )
}

suspend fun FirebaseUser.idToken(forceRefresh: Boolean): String = getIdToken(forceRefresh) ?: "invalid"

class Authentication {
    suspend fun idToken(forceRefresh: Boolean = false): String? {
        return Firebase.auth.currentUser?.getIdToken(forceRefresh)
    }

    fun currentUser(): User? {
        return Firebase.auth.currentUser?.toUser()
    }

    val currentUserFlow: Flow<User?> = Firebase.auth.authStateChanged.map {it?.toUser() }
        .onStart {
            emit(currentUser())
        }


    fun signOut() {
        // The underlying function is only blocking on JS so it should be OK to block here
        runBlocking {
            Firebase.auth.signOut()
        }
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
            val credential = GoogleAuthProvider.credential(account.idToken!!, null)
            scope.launch {
                val authResult = Firebase.auth.signInWithCredential(credential)
                onAuthComplete(authResult.user!!.toUser())
            }
        } catch (e: Exception) {
            onAuthError(e)
        }
    }
}
