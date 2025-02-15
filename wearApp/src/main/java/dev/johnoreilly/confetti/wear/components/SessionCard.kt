@file:OptIn(ExperimentalLayoutApi::class)

package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.RevealValue
import androidx.wear.compose.foundation.rememberRevealState
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.LocalContentColor
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SwipeToReveal
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SessionCard(
    session: SessionDetails,
    sessionSelected: (sessionId: String) -> Unit,
    currentTime: LocalDateTime,
    isBookmarked: Boolean,
    addBookmark: ((sessionId: String) -> Unit)?,
    removeBookmark: ((sessionId: String) -> Unit)?,
    modifier: Modifier = Modifier,
    timeDisplay: @Composable () -> Unit = {
        SessionTime(session, currentTime)
    }
) {
    val coroutineScope = rememberCoroutineScope()
    val revealState = rememberRevealState()

    if (session.isBreak()) {
        Text(session.title, modifier = modifier)
    } else if (addBookmark == null || removeBookmark == null) {
        SessionCardContent(modifier, sessionSelected, session, timeDisplay)
    } else {
        SwipeToReveal(
            modifier = modifier,
            revealState = revealState,
            actions = {
                primaryAction(
                    icon = {
                        Icon(
                            if (isBookmarked) Icons.Default.BookmarkAdded else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove Bookmark" else "Bookmark"
                        )
                    },
                    text = { Text(if (isBookmarked) "Unbookmark" else "Bookmark") },
                    label = if (isBookmarked) "Unbookmark" else "Bookmark",
                    onClick = {
                        if (isBookmarked) removeBookmark(session.id) else addBookmark(session.id)
                        coroutineScope.launch {
                            revealState.animateTo(RevealValue.Covered)
                        }
                    },
                )
            }
        ) {
            SessionCardContent(
                sessionSelected = sessionSelected,
                session = session,
                timeDisplay = timeDisplay
            )
        }
    }
}

@Composable
private fun SessionCardContent(
    modifier: Modifier = Modifier,
    sessionSelected: (sessionId: String) -> Unit,
    session: SessionDetails,
    timeDisplay: @Composable () -> Unit
) {
    TitleCard(
        modifier = modifier.fillMaxWidth(),
        onClick = { sessionSelected(session.id) },
        title = { Text(text = session.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
    ) {
        if (session.speakers.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    session.speakers.joinToString(", ") { it.speakerDetails.name },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Light,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.size(4.dp))
        Row {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                LocalTextStyle provides MaterialTheme.typography.labelMedium,
            ) {
                Text(
                    session.room?.name.orEmpty(),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )

                timeDisplay()
            }
        }
    }
}

@Composable
fun SessionTime(
    session: SessionDetails,
    currentTime: LocalDateTime
) {
    val timeFormatted = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    if (currentTime in session.startsAt..session.endsAt) {
        Text(stringResource(id = R.string.now), color = MaterialTheme.colorScheme.error)
    } else {
        Text(timeFormatted.format(session.startsAt.toJavaLocalDateTime()))
    }
}


