@file:Suppress("DEPRECATION")

package dev.johnoreilly.confetti.wear.auth

import com.arkivanov.decompose.ComponentContext
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.horologist.auth.ui.googlesignin.signin.GoogleSignInViewModel
import dev.johnoreilly.confetti.wear.data.auth.FirebaseAuthUserRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface FirebaseSignInComponent {
    fun onAuthCancelled()
    fun onAuthSucceed()

    val viewModel: GoogleSignInViewModel
}

class DefaultFirebaseSignInComponent(
    componentContext: ComponentContext,
    private val authCancelled: () -> Unit,
    private val authSucceed: () -> Unit,
) : FirebaseSignInComponent, KoinComponent, ComponentContext by componentContext {
    val googleSignInClient: GoogleSignInClient by inject()
    val authUserRepository: FirebaseAuthUserRepository by inject()

    override fun onAuthCancelled() {
        authCancelled()
    }

    override fun onAuthSucceed() {
        authSucceed()
    }

    override val viewModel: GoogleSignInViewModel = GoogleSignInViewModel(
        googleSignInClient,
        authUserRepository
    )
}
