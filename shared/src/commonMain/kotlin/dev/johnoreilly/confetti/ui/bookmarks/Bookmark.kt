package dev.johnoreilly.confetti.ui.bookmarks

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

@Composable
fun Bookmark(
    isBookmarked: Boolean,
    modifier: Modifier = Modifier,
    onBookmarkChange: (Boolean) -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isBookmarked) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "BookmarkScale"
    )

    IconToggleButton(
        checked = isBookmarked,
        onCheckedChange = onBookmarkChange,
        modifier = modifier.scale(scale),
    ) {
        if (isBookmarked) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = "remove bookmark",
                tint = MaterialTheme.colorScheme.primary,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = "add bookmark",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}