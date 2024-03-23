package dev.johnoreilly.confetti.wear.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ListHeaderDefaults.itemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    ResponsiveListHeader(
        modifier = modifier,
        contentPadding = itemPadding(),
        contentColor = MaterialTheme.colors.secondary
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ScreenHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    ResponsiveListHeader(
        modifier = modifier,
        contentPadding = firstItemPadding(),
        contentColor = MaterialTheme.colors.secondary
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
        )
    }
}
