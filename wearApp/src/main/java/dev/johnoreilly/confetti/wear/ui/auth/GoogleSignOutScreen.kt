@file:OptIn(ExperimentalHorologistBaseUiApi::class)

package dev.johnoreilly.confetti.wear.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import com.google.android.horologist.base.ui.ExperimentalHorologistBaseUiApi
import com.google.android.horologist.base.ui.components.ConfirmationDialog
import dev.johnoreilly.confetti.R
import org.koin.androidx.compose.getViewModel

@Composable
fun GoogleSignOutScreen(
    navigateUp: () -> Unit,
    viewModel: ConfettiGoogleSignOutViewModel = getViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state) {
        GoogleSignOutScreenState.Idle -> {
            SideEffect {
                viewModel.onIdleStateObserved()
            }

            LoadingView()
        }

        GoogleSignOutScreenState.Loading -> {
            LoadingView()
        }

        GoogleSignOutScreenState.Success -> {
            ConfirmationDialog(
                onTimeout = navigateUp
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.google_sign_out_success_message)
                )
            }
        }

        GoogleSignOutScreenState.Failed -> {
            SideEffect {
                navigateUp()
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
