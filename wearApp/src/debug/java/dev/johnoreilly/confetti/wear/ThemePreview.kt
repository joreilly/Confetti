@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.base.ui.components.StandardChip
import com.google.android.horologist.base.ui.components.StandardChipType
import com.google.android.horologist.compose.tools.ThemeValues
import com.google.android.horologist.compose.tools.WearPreview
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.components.SessionSpeakerChip
import dev.johnoreilly.confetti.wear.preview.ConfettiPreviewThemes
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme

@Composable
fun ThemePreview() {
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
                conference = "kotlinconf2023",
                speaker = TestFixtures.JohnOreilly.speakerDetails,
                navigateToSpeaker = {}
            )
        }
        item {
            StandardChip(
                label = "Secondary Chip",
                secondaryLabel = "with secondary label",
                onClick = { /*TODO*/ },
                chipType = StandardChipType.Secondary
            )
        }
        // Breaks screenshot tests
        //            item {
        //                PlaceholderChip(
        //                    modifier = Modifier.fillMaxWidth(),
        //                )
        //            }
        item {
            SessionCard(TestFixtures.sessionDetails) {}
        }
    }
}

@WearPreview
@Composable
fun ThemePreview(
    @PreviewParameter(ConfettiPreviewThemes::class) themeValues: ThemeValues
) {
    ConfettiTheme(colors = themeValues.colors) {
        Box(modifier = Modifier.size(221.dp, 400.dp)) {
            ThemePreview()
        }
    }
}
