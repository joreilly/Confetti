package dev.johnoreilly.confetti.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ConfettiHeader(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    val tonalElevation = LocalBackgroundTheme.current.tonalElevation
    ConfettiBackground(
        tonalElevation = if (tonalElevation == Dp.Unspecified) {
            BackgroundTheme.DEFAULT_TONAL_ELEVATION
        } else {
            tonalElevation
        },
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Column {
            Divider()
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon?.let { icon ->
                    Icon(
                        modifier = Modifier
                            .padding(end = 8.dp),
                        imageVector = icon,
                        contentDescription = null,
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Divider()
        }
    }
}
