package dev.johnoreilly.confetti.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.isLightning
import dev.johnoreilly.confetti.isService
import dev.johnoreilly.confetti.sessionSpeakers
import dev.johnoreilly.confetti.ui.bookmarks.Bookmark
import dev.johnoreilly.confetti.ui.SignInDialog

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

    var modifier = Modifier.fillMaxSize()
    val tonalElevation = if (!session.isService() && !session.isBreak()) {
        0.dp
    } else {
        2.dp
    }
    if (!session.isBreak()) {
        modifier = modifier.clickable(onClick = {
            sessionSelected(session.id)
        })
    }


    Surface(
        tonalElevation = tonalElevation
    ) {
        Row(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                session.room?.let { room ->
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            session.sessionSpeakers() ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            room.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                if (session.isLightning()) {
                    Surface(
                        modifier = Modifier.padding(top = 8.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                            Icon(Icons.Default.Bolt, "lightning")
                            Spacer(Modifier.width(4.dp))
                            Text("Lightning / ${session.startsAt.time}-${session.endsAt.time}")
                        }
                    }
                }
            }


            var showDialog by remember { mutableStateOf(false) }

            if (!session.isBreak()) {
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

            if (showDialog) {
                SignInDialog(
                    onDismissRequest = { showDialog = false },
                    onSignInClicked = onNavigateToSignIn
                )
            }
        }
    }

}
