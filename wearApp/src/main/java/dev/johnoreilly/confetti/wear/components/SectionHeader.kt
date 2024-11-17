package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListSubHeader

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    ListSubHeader(modifier =  modifier.fillMaxWidth()) {
        Text(
            text = text,
        )
    }
}

@Composable
fun ScreenHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    ListHeader(modifier = modifier.fillMaxWidth()) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
        )
    }
}
