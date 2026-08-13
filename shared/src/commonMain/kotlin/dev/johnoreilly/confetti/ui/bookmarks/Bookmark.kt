package dev.johnoreilly.confetti.ui.bookmarks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Bookmark(
    isBookmarked: Boolean,
    modifier: Modifier = Modifier,
    onBookmarkChange: (Boolean) -> Unit,
) {
    IconToggleButton(
        checked = isBookmarked,
        onCheckedChange = onBookmarkChange,
        modifier = modifier,
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