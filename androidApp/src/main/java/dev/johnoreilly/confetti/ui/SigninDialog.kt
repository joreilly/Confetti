package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.johnoreilly.confetti.R

@Composable
fun SignInDialog(onDismissRequest: () -> Unit, onSignInClicked: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {

        Card {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = stringResource(id = R.string.sign_in_for_bookmarks),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.align(CenterHorizontally)) {
                    Button(onClick = onDismissRequest) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = onSignInClicked) {
                        Text(text = stringResource(id = R.string.sign_in))
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun SignInDialogPreview() {
    SignInDialog({}, {})
}