package dev.johnoreilly.confetti.wear.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Confirmation
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import dev.johnoreilly.confetti.R

@Composable
fun FirebaseSignOutScreen(
    component: FirebaseSignOutComponent,
) {
    val state by component.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(timeText = {}) {
        when (state) {
            GoogleSignOutScreenState.Idle -> {
                SideEffect {
                    component.onIdleStateObserved()
                }

                LoadingView()
            }

            GoogleSignOutScreenState.Loading -> {
                LoadingView()
            }

            GoogleSignOutScreenState.Success -> {
                SideEffect {
                    component.signedOut()
                }
                Confirmation(
                    show = true,
                    onDismissRequest = {
                        component.navigateUp()
                    },
                    text = {
                        Text(
                            text = stringResource(id = R.string.google_sign_out_success_message)
                        )
                    }) {
                    // WAT!?
                }
            }

            GoogleSignOutScreenState.Failed -> {
                SideEffect {
                    component.navigateUp()
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
