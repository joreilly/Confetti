@file:Suppress("DEPRECATION")

package dev.johnoreilly.confetti.car.signin

import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dev.johnoreilly.confetti.car.R

const val BINDER_KEY = "binder"

/**
 * Sign in with Google adapted from Android car samples:
 * - https://github.com/androidx/androidx/
 */

class SignInWithGoogleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val signInCompleteCallback = intent.extras?.getBinder(BINDER_KEY) as OnSignInComplete

        val activityResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
            signInCompleteCallback.onSignInComplete(account)
            finish()
        }

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            signInCompleteCallback.onSignInComplete(account)
            finish()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(applicationContext.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        val signInClient = GoogleSignIn.getClient(this, gso)
        activityResultLauncher.launch(signInClient.signInIntent)
    }

    abstract class OnSignInComplete: Binder(), IBinder {

        abstract fun onSignInComplete(account: GoogleSignInAccount?)
    }
}