package dev.johnoreilly.confetti.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.ui.LocalBottomNavigationPadding
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    val bottomNavPadding = LocalBottomNavigationPadding.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = bottomNavPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.5.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
            )

            if (message != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(name = "Loading", widthDp = 200, heightDp = 200, showBackground = true)
@Composable
internal fun LoadingViewPreview() {
    LoadingView()
}

@Preview(name = "Loading with message", widthDp = 300, heightDp = 200, showBackground = true)
@Composable
internal fun LoadingViewWithMessagePreview() {
    LoadingView(message = "Loading sessions...")
}
