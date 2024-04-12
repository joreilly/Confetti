package dev.johnoreilly.confetti.ui

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.facebook
import confetti.shared.generated.resources.github
import confetti.shared.generated.resources.linkedin
import confetti.shared.generated.resources.twitter
import confetti.shared.generated.resources.web
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.fullNameAndCompany
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sessionStartDateTimeFormat
import sessionTimeFormat

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SessionDetailViewShared(
    conference: String,
    session: SessionDetails?,
    onSpeakerClick: (speakerId: String) -> Unit,
    onSocialLinkClicked: (String) -> Unit
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
                    Text(
                        text = session.title,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )

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

                    Text(
                        text = session.sessionDescription ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )

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
                        text = "Speakers",
                        icon = Icons.Filled.Person,
                    )

                    Spacer(modifier = Modifier.size(16.dp))

                    Column(modifier = Modifier.padding(contentPadding)) {
                        session.speakers.forEach { speaker ->
                            SessionSpeakerInfo(conference, speaker.speakerDetails, onSpeakerClick, onSocialLinkClicked)
                        }
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun SessionSpeakerInfo(
    conference: String,
    speaker: SpeakerDetails,
    onSpeakerClick: (speakerId: String) -> Unit,
    onSocialLinkClick: (String) -> Unit
) {
    Column(Modifier
        .padding(top = 16.dp)
        .clickable(role = Role.Button) { onSpeakerClick(speaker.id) }
    ) {
        Row {
            speaker.photoUrl?.let {
                val url = "https://confetti-app.dev/images/avatar/${conference}/${speaker.id}"
                AsyncImage(
                    model = url,
                    contentDescription = speaker.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp)
                        .clip(CircleShape)
                )
            }

            Column(Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = speaker.fullNameAndCompany(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                speaker.tagline?.let { tagline ->
                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                speaker.bio?.let { bio ->
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = bio,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }


                Row(
                    Modifier.padding(top = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    speaker.socials.forEach { socialsItem ->
                        SocialIcon(
                            modifier = Modifier.size(28.dp),
                            socialItem = socialsItem,
                            onClick = { onSocialLinkClick(socialsItem.url) }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun SocialIcon(
    modifier: Modifier = Modifier,
    resource: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit
) {
    val iconTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    IconButton(onClick = onClick) {
        Icon(
            modifier = modifier,
            painter = painterResource(resource),
            contentDescription = contentDescription,
            tint = iconTint //Color(0, 128, 255)
        )
    }
}


@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun SocialIcon(
    modifier: Modifier = Modifier,
    socialItem: SpeakerDetails.Social,
    onClick: () -> Unit
) {
    when (socialItem.name.lowercase()) {
        "github" -> SocialIcon(
            modifier = modifier,
            resource = Res.drawable.github,
            contentDescription = "Github",
            onClick = onClick
        )

        "linkedin" -> SocialIcon(
            modifier = modifier,
            resource = Res.drawable.linkedin,
            contentDescription = "LinkedIn",
            onClick = onClick
        )

        "twitter" -> SocialIcon(
            modifier = modifier,
            resource = Res.drawable.twitter,
            contentDescription = "Twitter",
            onClick = onClick
        )

        "facebook" -> SocialIcon(
            modifier = modifier,
            resource = Res.drawable.facebook,
            contentDescription = "Facebook",
            onClick = onClick
        )

        else -> SocialIcon(
            modifier = modifier,
            resource = Res.drawable.web,
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
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(10.dp)
        )
    }
}


private fun sessionTimeString(startsAt: LocalDateTime, endsAt: LocalDateTime): String {
    val startTimeDate = startsAt.sessionStartDateTimeFormat()
    val endsAtTime = endsAt.sessionTimeFormat()
    return "$startTimeDate - $endsAtTime"
}
