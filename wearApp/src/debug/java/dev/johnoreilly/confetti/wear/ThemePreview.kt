@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.material.Chip
import com.google.android.horologist.compose.tools.ThemeValues
import com.google.android.horologist.compose.tools.WearPreview
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.components.SessionSpeakerChip
import dev.johnoreilly.confetti.wear.preview.ConfettiPreviewThemes
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiThemeFixed
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime

@Composable
fun ThemePreview() {
    val now = remember { LocalDateTime.of(2020, 1, 1, 1, 1).toKotlinLocalDateTime() }

    LazyColumn {
        item {
            ListHeader {
                Text("Standard List Header")
            }
        }
        item {
            SectionHeader("Section Header")
        }
        item {
            Text("Confetti: building a Kotlin Multiplatform conference app in 40min")
        }
        item {
            SessionSpeakerChip(
                speaker = TestFixtures.JohnOreilly.speakerDetails,
                navigateToSpeaker = {}
            )
        }
        item {
            Chip(
                label = "Secondary Chip",
                secondaryLabel = "with secondary label",
                onClick = { /*TODO*/ },
                colors = ChipDefaults.secondaryChipColors()
            )
        }
        // Breaks screenshot tests
        //            item {
        //                PlaceholderChip(
        //                    modifier = Modifier.fillMaxWidth(),
        //                )
        //            }
        item {
            SessionCard(
                session = TestFixtures.sessionDetails,
                sessionSelected = {},
                currentTime = now
            )
        }
    }
}

@WearPreview
@Composable
fun ThemePreview(
    @PreviewParameter(ConfettiPreviewThemes::class) themeValues: ThemeValues
) {
    ConfettiThemeFixed(colors = themeValues.colors) {
        Box(modifier = Modifier.size(221.dp, 400.dp)) {
            ThemePreview()
        }
    }
}
