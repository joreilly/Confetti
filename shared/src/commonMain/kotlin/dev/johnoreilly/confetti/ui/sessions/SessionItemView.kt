package dev.johnoreilly.confetti.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.preview.breakSession
import dev.johnoreilly.confetti.preview.lightningSession
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.isLightning
import dev.johnoreilly.confetti.isService
import dev.johnoreilly.confetti.sessionSpeakers
import dev.johnoreilly.confetti.ui.SignInDialog
import dev.johnoreilly.confetti.ui.bookmarks.Bookmark

@Composable
fun SessionItemView(
    session: SessionDetails,
    sessionSelected: (sessionId: String) -> Unit,
    isBookmarked: Boolean,
    addBookmark: (String) -> Unit,
    removeBookmark: (String) -> Unit,
    onNavigateToSignIn: () -> Unit = {},
    isLoggedIn: Boolean,
) {
    if (session.isBreak() || session.isService()) {
        BreakSessionItemView(session = session)
    } else {
        TalkSessionItemView(
            session = session,
            sessionSelected = sessionSelected,
            isBookmarked = isBookmarked,
            addBookmark = addBookmark,
            removeBookmark = removeBookmark,
            onNavigateToSignIn = onNavigateToSignIn,
            isLoggedIn = isLoggedIn,
        )
    }
}

@Composable
private fun BreakSessionItemView(
    session: SessionDetails,
    modifier: Modifier = Modifier,
) {
    val icon = when {
        session.isBreak() -> Icons.Default.Coffee
        session.title.contains("party", ignoreCase = true) ||
            session.title.contains("reception", ignoreCase = true) -> Icons.Default.Celebration
        else -> Icons.Default.Info
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                val location = session.room?.name
                if (!location.isNullOrBlank()) {
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TalkSessionItemView(
    session: SessionDetails,
    sessionSelected: (sessionId: String) -> Unit,
    isBookmarked: Boolean,
    addBookmark: (String) -> Unit,
    removeBookmark: (String) -> Unit,
    onNavigateToSignIn: () -> Unit = {},
    isLoggedIn: Boolean,
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { sessionSelected(session.id) }),
        headlineContent = {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        supportingContent = {
            Column {
                val speakers = session.sessionSpeakers()
                if (!speakers.isNullOrEmpty()) {
                    Text(
                        text = speakers,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                session.room?.let { room ->
                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (session.isLightning()) {
                    Surface(
                        modifier = Modifier.padding(top = 8.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "lightning",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Lightning / ${session.startsAt.time}-${session.endsAt.time}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        },
        trailingContent = {
            Bookmark(
                isBookmarked = isBookmarked,
                onBookmarkChange = { shouldAdd ->
                    if (!isLoggedIn) {
                        showDialog = true
                        return@Bookmark
                    }
                    if (shouldAdd) {
                        addBookmark(session.id)
                    } else {
                        removeBookmark(session.id)
                    }
                }
            )
        }
    )

    if (showDialog) {
        SignInDialog(
            onDismissRequest = { showDialog = false },
            onSignInClicked = onNavigateToSignIn
        )
    }
}

@MobilePreviews
@Composable
internal fun SessionItemViewLoadedPreview() {
    SessionItemView(
        session = sessionDetails,
        sessionSelected = {},
        isBookmarked = true,
        addBookmark = {},
        removeBookmark = {},
        isLoggedIn = true,
    )
}

@Preview(name = "Lightning talk", widthDp = 411, heightDp = 220, showBackground = true)
@Composable
internal fun SessionItemViewLightningPreview() {
    SessionItemView(
        session = lightningSession,
        sessionSelected = {},
        isBookmarked = false,
        addBookmark = {},
        removeBookmark = {},
        isLoggedIn = false,
    )
}

@Preview(name = "Break", widthDp = 411, heightDp = 140, showBackground = true)
@Composable
internal fun SessionItemViewBreakPreview() {
    SessionItemView(
        session = breakSession,
        sessionSelected = {},
        isBookmarked = false,
        addBookmark = {},
        removeBookmark = {},
        isLoggedIn = false,
    )
}
