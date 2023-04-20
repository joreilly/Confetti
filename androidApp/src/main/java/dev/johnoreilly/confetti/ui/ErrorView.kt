package dev.johnoreilly.confetti.ui

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.R

@Composable
fun ErrorView(onRefresh: (() ->Unit)? = null){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.oops)
            )
            if (onRefresh != null) {
                Button(
                    onClick = onRefresh,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ) {
                    Text(text = stringResource(id = R.string.retry))
                }
            }
        }
    }
}