package dev.johnoreilly.confetti.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

// Theme specimens — the Confetti Wear type ramp and colour-scheme swatches read
// straight from [MaterialTheme], mirroring the wear-m3 sample's specimens but
// self-contained on Confetti's own theme.
@Preview(widthDp = 227)
@Composable
fun TypographySpecimenPreview() {
    ConfettiThemeFixed {
        Column {
            Text("Title Large", style = MaterialTheme.typography.titleLarge)
            Text("Title Medium", style = MaterialTheme.typography.titleMedium)
            Text("Body Large", style = MaterialTheme.typography.bodyLarge)
            Text("Label Medium", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Preview(widthDp = 227)
@Composable
fun ColorSchemeSpecimenPreview() {
    ConfettiThemeFixed {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary))
            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.secondary))
            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.surfaceContainer))
            Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.error))
        }
    }
}
