package dev.johnoreilly.confetti.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.johnoreilly.confetti.ui.component.FullScreenPhotoDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.speakers
import dev.johnoreilly.confetti.avatarUrl
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.fullNameAndCompany
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.icons.Facebook
import dev.johnoreilly.confetti.ui.icons.Github
import dev.johnoreilly.confetti.ui.icons.Linkedin
import dev.johnoreilly.confetti.ui.icons.Twitter
import dev.johnoreilly.confetti.ui.icons.Web
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import sessionStartDateTimeFormat
import sessionTimeFormat

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionDetailViewShared(
    conference: String,
    session: SessionDetails?,
    onSpeakerClick: (speakerId: String) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column {
        session?.let { session ->
            val contentPadding = remember { PaddingValues(horizontal = 16.dp) }
            Column(
                modifier = Modifier.fillMaxWidth()
                    .verticalScroll(state = scrollState)
            ) {
                Column(modifier = Modifier.padding(contentPadding)) {
                    SelectionContainer {
                        Text(
                            text = session.title,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.size(16.dp))

                    Text(
                        text = sessionTimeString(session.startsAt, session.endsAt),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    session.room?.name?.let { roomName ->
                        Text(
                            modifier = Modifier.padding(vertical = 2.dp),
                            text = roomName,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge.copy(fontStyle = FontStyle.Italic)
                        )
                    }

                    Spacer(modifier = Modifier.size(16.dp))

                    SelectionContainer {
                        Text(
                            text = session.sessionDescription ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (session.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(16.dp))
                        FlowRow {
                            session.tags.distinct().forEach { tag ->
                                Box(Modifier.padding(bottom = 8.dp)) {
                                    Chip(tag)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                }

                if (session.speakers.isNotEmpty()) {
                    ConfettiHeader(
                        text = stringResource(Res.string.speakers),
                        icon = Icons.Filled.Person,
                    )

                    Spacer(modifier = Modifier.size(16.dp))

                    Column(modifier = Modifier.padding(contentPadding)) {
                        session.speakers.forEach { speaker ->
                            SessionSpeakerInfo(conference, speaker.speakerDetails, onSpeakerClick)
                        }
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                }

                session.recordingUrl?.let { recordingUrl ->
                    val uriHandler = LocalUriHandler.current

                    ConfettiHeader(
                        text = "Recording",
                        icon = Icons.Filled.PlayCircle,
                    )

                    Spacer(modifier = Modifier.size(16.dp))

                    Column(modifier = Modifier.padding(contentPadding)) {
                        Button(onClick = { uriHandler.openUri(recordingUrl)}) {
                            Text("Watch Recording")
                        }
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
internal fun SessionSpeakerInfo(
    conference: String,
    speaker: SpeakerDetails,
    onSpeakerClick: (speakerId: String) -> Unit,
) {
    var showFullScreenPhoto by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onSpeakerClick(speaker.id) },
        headlineContent = {
            Text(
                text = speaker.fullNameAndCompany(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = if (speaker.tagline != null) {
            {
                Text(
                    text = speaker.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            null
        },
        leadingContent = {
            speaker.avatarUrl()?.let { url ->
                SubcomposeAsyncImage(
                    model = url,
                    contentDescription = speaker.name,
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable { showFullScreenPhoto = true }
                )
            }
        }
    )

    if (showFullScreenPhoto) {
        FullScreenPhotoDialog(
            photoUrl = speaker.avatarUrl(),
            contentDescription = speaker.name,
            onDismissRequest = { showFullScreenPhoto = false }
        )
    }
}


@Composable
internal fun SocialIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val iconTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    IconButton(onClick = onClick) {
        Icon(
            modifier = modifier,
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = iconTint //Color(0, 128, 255)
        )
    }
}


@Composable
internal fun SocialIcon(
    modifier: Modifier = Modifier,
    socialItem: SpeakerDetails.Social,
    onClick: () -> Unit
) {
    when (socialItem.name.lowercase()) {
        "github" -> SocialIcon(
            modifier = modifier,
            imageVector = ConfettiIcons.Github,
            contentDescription = "Github",
            onClick = onClick
        )

        "linkedin" -> SocialIcon(
            modifier = modifier,
            imageVector = ConfettiIcons.Linkedin,
            contentDescription = "LinkedIn",
            onClick = onClick
        )

        "twitter" -> SocialIcon(
            modifier = modifier,
            imageVector = ConfettiIcons.Twitter,
            contentDescription = "Twitter",
            onClick = onClick
        )

        "facebook" -> SocialIcon(
            modifier = modifier,
            imageVector = ConfettiIcons.Facebook,
            contentDescription = "Facebook",
            onClick = onClick
        )

        else -> SocialIcon(
            modifier = modifier,
            imageVector = ConfettiIcons.Web,
            contentDescription = "Web",
            onClick = onClick
        )
    }
}


@Composable
internal fun Chip(name: String) {
    Surface(
        modifier = Modifier.padding(end = 10.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(10.dp)
        )
    }
}


private fun sessionTimeString(startsAt: LocalDateTime, endsAt: LocalDateTime): String {
    val startTimeDate = startsAt.sessionStartDateTimeFormat()
    val endsAtTime = endsAt.sessionTimeFormat()
    return "$startTimeDate - $endsAtTime"
}

@MobilePreviews
@Composable
internal fun SessionDetailViewLoadedPreview() {
    MaterialTheme {
        SessionDetailViewShared(
            conference = "kotlinconf2023",
            session = sessionDetails,
            onSpeakerClick = {},
        )
    }
}

@Preview(name = "Empty", widthDp = 411, heightDp = 914, showBackground = true)
@Composable
internal fun SessionDetailViewEmptyPreview() {
    MaterialTheme {
        SessionDetailViewShared(
            conference = "kotlinconf2023",
            session = null,
            onSpeakerClick = {},
        )
    }
}
