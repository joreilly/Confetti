package dev.johnoreilly.confetti.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPlaceholderState
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.icons.Github
import dev.johnoreilly.confetti.wear.components.PlaceholderButton
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.ScreenHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.components.SessionSpeakerChip
import dev.johnoreilly.confetti.wear.home.DayChip
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.component.SocialIcon
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Single-component "sticker sheet" catalog. Each `@Preview` wraps exactly one
 * component (or one theme specimen) in [ConfettiThemeFixed] + [TestFixtures],
 * mirroring `ThemePreview.kt` but rendered from the **main** source set so the
 * `ee.schimke.composeai.preview` plugin discovers them for the published
 * design catalog. Screen-level previews live next to their screens; this file
 * is the component + theme layer.
 */

private val catalogNow =
    LocalDateTime.of(2020, 1, 1, 1, 1).toKotlinLocalDateTime()

@Preview(widthDp = 227)
@Composable
fun SessionCardPopulatedPreview() {
    ConfettiThemeFixed {
        SessionCard(
            session = TestFixtures.sessionDetails,
            sessionSelected = {},
            currentTime = catalogNow,
            isBookmarked = false,
            addBookmark = {},
            removeBookmark = {},
        )
    }
}

@Preview(widthDp = 227)
@Composable
fun SessionCardLoadingPreview() {
    ConfettiThemeFixed {
        SessionCard(
            session = null,
            sessionSelected = {},
            currentTime = null,
            isBookmarked = false,
            addBookmark = {},
            removeBookmark = {},
            placeholderState = rememberPlaceholderState(true),
        )
    }
}

@Preview(widthDp = 227)
@Composable
fun SessionCardBookmarkedPreview() {
    ConfettiThemeFixed {
        SessionCard(
            session = TestFixtures.sessionDetails,
            sessionSelected = {},
            currentTime = catalogNow,
            isBookmarked = true,
            addBookmark = {},
            removeBookmark = {},
        )
    }
}

@Preview(widthDp = 227)
@Composable
fun SessionSpeakerChipPreview() {
    ConfettiThemeFixed {
        SessionSpeakerChip(
            speaker = TestFixtures.JohnOreilly.speakerDetails,
            navigateToSpeaker = {},
        )
    }
}

@Preview(widthDp = 227)
@Composable
fun SectionHeaderPreview() {
    ConfettiThemeFixed {
        SectionHeader("Bookmarked sessions")
    }
}

@Preview(widthDp = 227)
@Composable
fun ScreenHeaderPreview() {
    ConfettiThemeFixed {
        ScreenHeader("KotlinConf 2023")
    }
}

@Preview(widthDp = 227)
@Composable
fun PlaceholderButtonPreview() {
    ConfettiThemeFixed {
        PlaceholderButton()
    }
}

@Preview(widthDp = 227)
@Composable
fun SocialIconPreview() {
    ConfettiThemeFixed {
        SocialIcon(
            imageVector = ConfettiIcons.Github,
            contentDescription = "Github",
            onClick = {},
        )
    }
}

@Preview(widthDp = 227)
@Composable
fun DayChipPreview() {
    val dayFormatter = remember { DateTimeFormatter.ofPattern("cccc") }
    ConfettiThemeFixed {
        DayChip(
            dayFormatter = dayFormatter,
            date = catalogNow.date,
            daySelected = {},
        )
    }
}

// Theme specimens — the Confetti Wear type ramp and colour-scheme roles read straight
// from [MaterialTheme]. Kept deliberately fuller than a bare list so the published Themes
// tab actually documents the scale and the colour roles (each labelled with its token and,
// for colours, an on-role glyph proving contrast), mirroring the richer phone catalog.

/** One labelled step of the type ramp: the sample rendered in [style], its token name muted below. */
@Composable
private fun TypeRow(sample: String, token: String, style: androidx.compose.ui.text.TextStyle) {
    Column {
        Text(sample, style = style, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            token,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Preview(widthDp = 227)
@Composable
fun TypographySpecimenPreview() {
    ConfettiThemeFixed {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            TypeRow("Confetti", "displaySmall", MaterialTheme.typography.displaySmall)
            TypeRow("Title Large", "titleLarge", MaterialTheme.typography.titleLarge)
            TypeRow("Title Medium", "titleMedium", MaterialTheme.typography.titleMedium)
            TypeRow("Body large text", "bodyLarge", MaterialTheme.typography.bodyLarge)
            TypeRow("Body medium text", "bodyMedium", MaterialTheme.typography.bodyMedium)
            TypeRow("LABEL MEDIUM", "labelMedium", MaterialTheme.typography.labelMedium)
        }
    }
}

/** A single colour-role swatch: the role colour with its on-role glyph, token name muted below. */
@Composable
private fun ColorSwatch(color: Color, onColor: Color, token: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Text("Aa", color = onColor, style = MaterialTheme.typography.labelMedium)
        }
        Text(
            token,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(58.dp),
        )
    }
}

@Preview(widthDp = 227)
@Composable
fun ColorSchemeSpecimenPreview() {
    val scheme = MaterialTheme.colorScheme
    ConfettiThemeFixed {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(scheme.background)
                .padding(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSwatch(scheme.primary, scheme.onPrimary, "primary")
                ColorSwatch(scheme.secondary, scheme.onSecondary, "secondary")
                ColorSwatch(scheme.tertiary, scheme.onTertiary, "tertiary")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorSwatch(scheme.primaryContainer, scheme.onPrimaryContainer, "prim cont")
                ColorSwatch(scheme.surfaceContainer, scheme.onSurface, "surface")
                ColorSwatch(scheme.error, scheme.onError, "error")
            }
        }
    }
}
