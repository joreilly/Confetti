package dev.johnoreilly.confetti.wear.auth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.horologist.auth.composables.dialogs.SignedInConfirmationDialog
import com.google.android.horologist.auth.composables.screens.AuthErrorScreen
import com.google.android.horologist.auth.ui.googlesignin.signin.GoogleSignInScreen

@Composable
fun FirebaseSignInScreen(
    component: FirebaseSignInComponent,
) {
    GoogleSignInScreen(
        onAuthCancelled = {
            component.onAuthCancelled()
        },
        failedContent = {
            AuthErrorScreen()
        },
        viewModel = component.viewModel,
    ) { successState ->
        SignedInConfirmationDialog(
            onDismissOrTimeout = { component.onAuthSucceed() },
            modifier = Modifier.fillMaxSize(),
            accountUiModel = successState.accountUiModel,
        )
    }
}