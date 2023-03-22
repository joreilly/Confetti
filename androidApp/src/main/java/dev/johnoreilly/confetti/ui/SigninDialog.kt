package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun SignInDialog(onDismissRequest: () -> Unit, onSignInClicked: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {

        Card {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "In order to save your bookmarks, you'll need to sign in.",
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row (modifier = Modifier.align(CenterHorizontally)){
                    Button(onClick = onDismissRequest) {
                        Text(text = "CANCEL")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = onSignInClicked) {
                        Text(text = "SIGN IN")
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