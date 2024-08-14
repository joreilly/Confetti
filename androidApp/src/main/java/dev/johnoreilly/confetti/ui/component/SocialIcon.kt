package dev.johnoreilly.confetti.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.icons.Facebook
import dev.johnoreilly.confetti.ui.icons.Github
import dev.johnoreilly.confetti.ui.icons.Linkedin
import dev.johnoreilly.confetti.ui.icons.Twitter
import dev.johnoreilly.confetti.ui.icons.Web

@Composable
fun SocialIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val iconTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    IconButton(onClick = onClick) {
        Icon(
            modifier = modifier.size(24.dp),
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = iconTint
        )
    }
}


@Composable
fun SocialIcon(
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
