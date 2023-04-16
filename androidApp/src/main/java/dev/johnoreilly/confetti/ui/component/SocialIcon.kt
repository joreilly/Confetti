package dev.johnoreilly.confetti.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SpeakerDetails

@Composable
fun SocialIcon(
    modifier: Modifier = Modifier,
    @DrawableRes resourceId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    val iconTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    IconButton(onClick = onClick) {
        Icon(
            modifier = modifier
                .size(24.dp),
            painter = painterResource(resourceId),
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
            resourceId = dev.johnoreilly.confetti.shared.R.drawable.github,
            contentDescription = "Github",
            onClick = onClick
        )

        "linkedin" -> SocialIcon(
            modifier = modifier,
            resourceId = dev.johnoreilly.confetti.shared.R.drawable.ic_network_linkedin,
            contentDescription = "LinkedIn",
            onClick = onClick
        )

        "twitter" -> SocialIcon(
            modifier = modifier,
            resourceId = dev.johnoreilly.confetti.shared.R.drawable.ic_network_twitter,
            contentDescription = "Twitter",
            onClick = onClick
        )

        "facebook" -> SocialIcon(
            modifier = modifier,
            resourceId = dev.johnoreilly.confetti.shared.R.drawable.ic_network_facebook,
            contentDescription = "Facebook",
            onClick = onClick
        )

        else -> SocialIcon(
            modifier = modifier,
            resourceId = dev.johnoreilly.confetti.shared.R.drawable.ic_network_web,
            contentDescription = "Web",
            onClick = onClick
        )
    }
}
