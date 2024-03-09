@file:OptIn(ExperimentalLayoutApi::class)

package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.LocalContentColor
import androidx.wear.compose.material.LocalTextStyle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SessionCard(
    session: SessionDetails,
    sessionSelected: (sessionId: String) -> Unit,
    currentTime: LocalDateTime,
    modifier: Modifier = Modifier,
    timeDisplay: @Composable () -> Unit = {
        SessionTime(session, currentTime)
    }
) {
    if (session.isBreak()) {
        Text(session.title, modifier = modifier)
    } else {
        TitleCard(
            modifier = modifier.fillMaxWidth(),
            onClick = { sessionSelected(session.id) },
            title = { Text(text = session.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
            backgroundPainter = CardDefaults.cardBackgroundPainter(),
            contentColor = MaterialTheme.colors.onSurfaceVariant,
            titleColor = MaterialTheme.colors.onSurface
        ) {
            if (session.speakers.isNotEmpty()) {
                Spacer(modifier = Modifier.size(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), ) {
                    session.speakers.forEach { speaker ->
                        SpeakerLabel(speaker)
                    }
                }
            }
            Spacer(modifier = Modifier.size(4.dp))
            Row {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colors.onSurfaceVariant,
                    LocalTextStyle provides MaterialTheme.typography.caption1,
                ) {
                    Text(session.room?.name ?: "", modifier = Modifier.weight(1f))

                    timeDisplay()
                }
            }
        }
    }
}

@Composable
fun SpeakerLabel(speaker: SessionDetails.Speaker) {
    Row {
        // TODO add avatar
        Text(
            speaker.speakerDetails.name,
            style = MaterialTheme.typography.caption2,
            fontWeight = FontWeight.Light,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SessionTime(
    session: SessionDetails,
    currentTime: LocalDateTime
) {
    val timeFormatted = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    if (currentTime in session.startsAt..session.endsAt) {
        Text(stringResource(id = R.string.now), color = MaterialTheme.colors.error)
    } else {
        Text(timeFormatted.format(session.startsAt.toJavaLocalDateTime()))
    }
}


