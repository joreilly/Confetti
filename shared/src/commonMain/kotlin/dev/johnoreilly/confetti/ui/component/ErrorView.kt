package dev.johnoreilly.confetti.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.oops
import confetti.shared.generated.resources.retry
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorView(onRefresh: (() ->Unit)? = null){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Column {
            Text(
                text = stringResource(Res.string.oops)
            )
            if (onRefresh != null) {
                Button(
                    onClick = onRefresh,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ) {
                    Text(text = stringResource(Res.string.retry))
                }
            }
        }
    }
}

@Preview(name = "Error with retry", widthDp = 360, heightDp = 200, showBackground = true)
@Composable
internal fun ErrorViewWithRetryPreview() {
    ErrorView(onRefresh = {})
}

@Preview(name = "Error without retry", widthDp = 360, heightDp = 200, showBackground = true)
@Composable
internal fun ErrorViewNoRetryPreview() {
    ErrorView(onRefresh = null)
}