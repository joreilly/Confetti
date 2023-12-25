package dev.johnoreilly.confetti.wear

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import com.google.android.horologist.compose.material.Chip
import dev.johnoreilly.confetti.wear.components.SectionHeader
import dev.johnoreilly.confetti.wear.components.SessionCard
import dev.johnoreilly.confetti.wear.components.SessionSpeakerChip
import dev.johnoreilly.confetti.wear.preview.TestFixtures
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
                navigateToSpeaker = {},
                conference =  TestFixtures.kotlinConf2023.id
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
