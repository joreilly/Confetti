package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.rememberExpandableState
import androidx.wear.compose.material.LocalContentColor
import androidx.wear.compose.material.LocalTextStyle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import coil.compose.AsyncImage
import com.google.android.horologist.compose.tools.ThemeValues
import com.google.android.horologist.compose.tools.WearPreview
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.wear.preview.ConfettiPreviewThemes
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
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
    expanded: Boolean = false,
    onExpand: (() -> Unit)? = null,
    timeDisplay: @Composable () -> Unit = {
        SessionTime(session, currentTime)
    }
) {
    if (session.isBreak()) {
        Text(session.title, modifier = modifier)
    } else {
        SpecificSessionCard(
            sessionSelected = sessionSelected,
            session = session,
            currentTime = currentTime,
            modifier = modifier,
            timeDisplay = timeDisplay,
            expanded = expanded,
            onExpand = onExpand
        )
    }
}

@Composable
private fun SpecificSessionCard(
    sessionSelected: (sessionId: String) -> Unit,
    session: SessionDetails,
    currentTime: LocalDateTime,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    onExpand: (() -> Unit)? = null,
    timeDisplay: @Composable () -> Unit = {
        SessionTime(session, currentTime)
    }
) {
    TitleCard(modifier = modifier.fillMaxWidth(),
        onClick = { sessionSelected(session.id) },
        title = { Text(text = session.title, maxLines = if (expanded) 4 else 2, overflow = TextOverflow.Ellipsis) }) {
        if (expanded) {
            if (session.speakers.isNotEmpty()) {
                Spacer(modifier = Modifier.size(4.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    session.speakers.forEach { speaker ->
                        SpeakerLabel(speaker)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.size(4.dp))
        val roomRowModifier = if (onExpand != null) {
            Modifier.clickable(onClick = onExpand, onClickLabel = "Expand Session Card")
        } else {
            Modifier
        }
        Row(modifier = roomRowModifier) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colors.onSurfaceVariant,
                LocalTextStyle provides MaterialTheme.typography.caption1,
            ) {
                Text(session.room?.name ?: "", modifier = Modifier.weight(1f), maxLines = if (expanded) 2 else 1)

                if (onExpand != null) {
                    Image(
                        imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = null
                    )
                }

                timeDisplay()
            }
        }
    }
}

@Composable
fun SpeakerLabel(speaker: SessionDetails.Speaker) {
    Row {
        AsyncImage(
            model = speaker.speakerDetails.photoUrl,
            contentDescription = "",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Text(
            speaker.speakerDetails.name,
            style = MaterialTheme.typography.caption1,
            fontWeight = FontWeight.Light,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SessionTime(
    session: SessionDetails, currentTime: LocalDateTime
) {
    val timeFormatted = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    if (currentTime in session.startsAt..session.endsAt) {
        Text(stringResource(id = R.string.now), color = MaterialTheme.colors.error)
    } else {
        Text(timeFormatted.format(session.startsAt.toJavaLocalDateTime()))
    }
}

@WearPreview
@Composable
fun SessionCardExpandedPreview(
    @PreviewParameter(ConfettiPreviewThemes::class) themeValues: ThemeValues
) {
    Box(modifier = Modifier.width(221.dp)) {
        ConfettiThemeFixed(colors = themeValues.colors) {
            SessionCard(
                session = TestFixtures.sessionDetails,
                sessionSelected = {},
                expanded = true,
                currentTime = LocalDateTime.parse("2020-01-01T01:01:01")
            )
        }
    }
}

@WearPreview
@Composable
fun SessionCardCollapsedPreview(
    @PreviewParameter(ConfettiPreviewThemes::class) themeValues: ThemeValues
) {
    Box(modifier = Modifier.width(221.dp)) {
        ConfettiThemeFixed(colors = themeValues.colors) {
            SessionCard(
                session = TestFixtures.sessionDetails,
                sessionSelected = {},
                expanded = false,
                currentTime = LocalDateTime.parse("2020-01-01T01:01:01")
            )
        }
    }
}

@WearPreview
@Composable
fun SessionCardCollapsedExpandablePreview(
    @PreviewParameter(ConfettiPreviewThemes::class) themeValues: ThemeValues
) {
    val state = rememberExpandableState()
    Box(modifier = Modifier.width(221.dp)) {
        ConfettiThemeFixed(colors = themeValues.colors) {
            SessionCard(
                session = TestFixtures.sessionDetails,
                sessionSelected = {},
                expanded = state.expanded,
                onExpand = { state.expanded = true },
                currentTime = LocalDateTime.parse("2020-01-01T01:01:01")
            )
        }
    }
}
