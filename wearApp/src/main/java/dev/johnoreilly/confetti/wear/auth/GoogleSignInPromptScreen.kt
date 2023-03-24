@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.auth.composables.chips.SignInChip
import com.google.android.horologist.auth.ui.common.screens.prompt.SignInPromptScreen
import com.google.android.horologist.auth.ui.common.screens.prompt.SignInPromptViewModel
import com.google.android.horologist.base.ui.components.ConfirmationDialog
import com.google.android.horologist.base.ui.components.StandardChipType
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import dev.johnoreilly.confetti.R
import org.koin.androidx.compose.getViewModel

@Composable
fun GoogleSignInPromptScreen(
    navigateToGoogleSignIn: () -> Unit,
    navigateUp: () -> Unit,
    columnState: ScalingLazyColumnState,
    modifier: Modifier = Modifier,
    viewModel: SignInPromptViewModel = getViewModel()
) {
    var showAlreadySignedInDialog by rememberSaveable { mutableStateOf(false) }

    SignInPromptScreen(
        message = stringResource(id = R.string.google_sign_in_prompt_message),
        onAlreadySignedIn = {
            showAlreadySignedInDialog = true
        },
        columnState = columnState,
        modifier = modifier,
        viewModel = viewModel
    ) {
        item {
            SignInChip(
                onClick = {
                    navigateToGoogleSignIn()
                },
                chipType = StandardChipType.Secondary
            )
        }
    }

    if (showAlreadySignedInDialog) {
        ConfirmationDialog(
            onTimeout = {
                showAlreadySignedInDialog = false
                navigateUp()
            }
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.google_sign_in_prompt_already_signed_in_message)
            )
        }
    }
}
