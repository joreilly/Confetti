package dev.johnoreilly.confetti.wear.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import dev.johnoreilly.confetti.ui.ConferenceCard
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.components.SessionSpeakerChip
import dev.johnoreilly.confetti.wear.home.DayChip
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WearPreviewThemes : PreviewParameterProvider<Theme> {
    override val values: Sequence<Theme>
        get() = listOf(null, "0x800000", "0x008000", "0x000080").map {
            Theme(it.orEmpty(), it.toColor())
        }.asSequence()
}

data class Theme(val name: String, val color: Color?) {
    constructor(color: String) : this(color, color.toColor())
}

@Composable
fun ThemePreview(seedColor: Theme) {
    ConfettiTheme(seedColor = seedColor.color) {
        val dayFormatter = remember { DateTimeFormatter.ofPattern("cccc") }
        val now = remember { LocalDateTime.of(2020, 1, 1, 1, 1).toKotlinLocalDateTime() }

        Column {
            ListHeader {
                Text("Standard List Header")
            }
            DayChip(dayFormatter = dayFormatter, date = now.date, daySelected = {})
            SectionHeader("Section Header")
            Text("Confetti: building a Kotlin Multiplatform conference app in 40min")
            ConferenceCard(conference = TestFixtures.conferences.first(), navigateToConference = {})
            SessionSpeakerChip(speaker = TestFixtures.JohnOreilly.speakerDetails, navigateToSpeaker = {})
            SessionCard(
                session = TestFixtures.sessionDetails,
                sessionSelected = {},
                currentTime = now,
                addBookmark = {},
                removeBookmark = {},
                isBookmarked = false
            )
        }
    }
}

@Preview(widthDp = 227)
@Composable
fun Defaults() {
    ThemePreview(seedColor = Theme("", null))
}

@Preview(widthDp = 227)
@Composable
fun NotSet() {
    ThemePreview(seedColor = Theme("", Color.Black))
}

@Preview(widthDp = 227)
@Composable
fun Color_0x800000() {
    ThemePreview(seedColor = Theme("0x800000"))
}

@Preview(widthDp = 227)
@Composable
fun Color_0x008000() {
    ThemePreview(seedColor = Theme("0x008000"))
}

@Preview(widthDp = 227)
@Composable
fun Color_0x000080() {
    ThemePreview(seedColor = Theme("0x000080"))
}
