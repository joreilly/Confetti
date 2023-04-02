package dev.johnoreilly.confetti.wear.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SpeakerDetails

@Composable
fun SocialIcon(
    modifier: Modifier = Modifier,
    @DrawableRes resourceId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    val iconTint = MaterialTheme.colors.onSurface.copy(alpha = 0.75f)
    Icon(
        modifier = modifier
            .size(24.dp)
            .clickable(onClick = onClick),
        painter = painterResource(resourceId),
        contentDescription = contentDescription,
        tint = iconTint
    )
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
            resourceId = R.drawable.github,
            contentDescription = "Github",
            onClick = onClick
        )
        "linkedin" -> SocialIcon(
            modifier = modifier,
            resourceId = R.drawable.ic_network_linkedin,
            contentDescription = "LinkedIn",
            onClick = onClick
        )
        "twitter" -> SocialIcon(
            modifier = modifier,
            resourceId = R.drawable.ic_network_twitter,
            contentDescription = "Twitter",
            onClick = onClick
        )
        "facebook" -> SocialIcon(
            modifier = modifier,
            resourceId = R.drawable.ic_network_facebook,
            contentDescription = "Facebook",
            onClick = onClick
        )
        else -> SocialIcon(
            modifier = modifier,
            resourceId = R.drawable.ic_network_web,
            contentDescription = "Web",
            onClick = onClick
        )
    }
}
