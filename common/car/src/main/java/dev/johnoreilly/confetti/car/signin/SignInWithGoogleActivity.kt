package dev.johnoreilly.confetti.car.signin

import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dev.johnoreilly.confetti.account.SignInProcess
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val BINDER_KEY = "binder"

/**
 * Sign in with Google for the car screens, which can't show the sign-in UI themselves.
 *
 * Uses Credential Manager through the shared [SignInProcess], like the phone and Wear apps; the
 * legacy GoogleSignIn API this used to call was removed in play-services-auth 22.
 */
class SignInWithGoogleActivity : ComponentActivity(), KoinComponent {
    private val signInProcess: SignInProcess by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val signInCompleteCallback = intent.extras?.getBinder(BINDER_KEY) as OnSignInComplete

        lifecycleScope.launch {
            val signedIn = signInProcess.signIn(this@SignInWithGoogleActivity)
            signInCompleteCallback.onSignInComplete(signedIn)
            finish()
        }
    }

    abstract class OnSignInComplete: Binder(), IBinder {
        abstract fun onSignInComplete(signedIn: Boolean)
    }
}
