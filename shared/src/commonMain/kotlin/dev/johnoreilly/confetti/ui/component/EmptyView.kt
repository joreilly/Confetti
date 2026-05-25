package dev.johnoreilly.confetti.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun EmptyView(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            fontSize = 24.sp,
            text = text
        )
    }
}

@Preview(name = "Empty", widthDp = 360, heightDp = 200, showBackground = true)
@Composable
internal fun EmptyViewPreview() {
    EmptyView(text = "No bookmarks yet")
}
