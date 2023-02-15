package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seiko.imageloader.rememberImagePainter
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.fullNameAndCompany
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.utils.DateService
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject


@Composable
internal fun SessionDetailViewShared(session: SessionDetails?, socialLinkClicked: (String) -> Unit) {
    val dateService = koinInject<DateService>()
    val scrollState = rememberScrollState()

    Column {
        session?.let { session ->
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(state = scrollState)
            ) {
                Text(
                    text = session.title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = sessionTimeString(dateService, session.startsAt, session.endsAt),
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
                    FlowRow(crossAxisSpacing = 8.dp) {
                        session.tags.distinct().forEach { tag ->
                            Box(Modifier.padding(bottom = 8.dp)) {
                                Chip(tag)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                }

                ConfettiHeader(
                    text = "Speakers",
                    icon = Icons.Filled.Person,
                )

                Spacer(modifier = Modifier.size(16.dp))

                session.speakers.forEach { speaker ->
                    SessionSpeakerInfo(speaker = speaker.speakerDetails, onSocialLinkClick = socialLinkClicked)
                }
            }
        }
    }
}



@Composable
internal fun SessionSpeakerInfo(
    speaker: SpeakerDetails,
    onSocialLinkClick: (String) -> Unit
) {
    Column(Modifier.padding(top = 16.dp)) {
        Row {
            speaker.photoUrl?.let {
                val painter = rememberImagePainter(speaker.photoUrl)
                Image(
                    painter, null,
                    modifier = Modifier.size(64.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
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
                        style = MaterialTheme.typography.bodySmall
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
    resource: String,
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


@Composable
internal fun SocialIcon(
    modifier: Modifier = Modifier,
    socialItem: SpeakerDetails.Social,
    onClick: () -> Unit
) {
    when (socialItem.name.lowercase()) {
        "github" -> SocialIcon(
            modifier = modifier,
            resource = "github.xml",
            contentDescription = "Github",
            onClick = onClick
        )

        "linkedin" -> SocialIcon(
            modifier = modifier,
            resource = "linkedin.xml",
            contentDescription = "LinkedIn",
            onClick = onClick
        )

        "twitter" -> SocialIcon(
            modifier = modifier,
            resource = "twitter.xml",
            contentDescription = "Twitter",
            onClick = onClick
        )

        "facebook" -> SocialIcon(
            modifier = modifier,
            resource = "facebook.xml",
            contentDescription = "Facebook",
            onClick = onClick
        )

        else -> SocialIcon(
            modifier = modifier,
            resource = "web.xml",
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


private fun sessionTimeString(dateService: DateService, startsAt: LocalDateTime, endsAt: LocalDateTime): String {
    val startTimeDate = dateService.format(startsAt, TimeZone.currentSystemDefault(), "MMM d HH:mm")
    val endsAtTime = dateService.format(endsAt, TimeZone.currentSystemDefault(), "HH:mm")
    return "$startTimeDate - $endsAtTime"
}
