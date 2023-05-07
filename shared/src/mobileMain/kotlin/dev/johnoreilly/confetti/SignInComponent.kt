package dev.johnoreilly.confetti

import com.arkivanov.decompose.ComponentContext
import dev.johnoreilly.confetti.auth.Authentication
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SignInComponent {

    fun signIn(idToken: String)
    fun onCloseClicked()
}

class DefaultSignInComponent(
    componentContext: ComponentContext,
    private val onClosed: () -> Unit,
) : SignInComponent, KoinComponent, ComponentContext by componentContext {

    private val authentication: Authentication by inject()
    private val coroutineScope = coroutineScope()

    override fun signIn(idToken: String) {
        coroutineScope.launch {
            authentication.signIn(idToken)
        }
    }

    override fun onCloseClicked() {
        onClosed()
    }
}
