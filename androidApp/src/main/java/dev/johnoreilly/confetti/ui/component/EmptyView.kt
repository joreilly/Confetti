package dev.johnoreilly.confetti.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import dev.johnoreilly.confetti.R

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            fontSize = 24.sp,
            text = stringResource(id = R.string.empty_text)
        )
    }
}
