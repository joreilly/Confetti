package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Bookmark(
    isBookmarked: Boolean,
    modifier: Modifier = Modifier,
    onBookmarkClick: () -> Unit,
) {

    if (isBookmarked) {
        Icon(
            imageVector = Icons.Outlined.Bookmark,
            contentDescription = "remove bookmark",
            tint = MaterialTheme.colorScheme.primary,
            modifier = modifier
                .clickable { onBookmarkClick() }
                .padding(8.dp))
    } else {
        Icon(
            imageVector = Icons.Outlined.BookmarkAdd,
            contentDescription = "add bookmark",
            modifier = Modifier
                .clickable { onBookmarkClick() }
                .padding(8.dp))
    }
}