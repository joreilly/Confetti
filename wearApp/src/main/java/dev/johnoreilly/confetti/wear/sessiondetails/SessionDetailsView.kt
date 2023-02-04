@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.sessiondetails

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.foundation.lazy.items
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.utils.AndroidDateService
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import dev.johnoreilly.confetti.wear.ui.previews.WearSmallRoundDevicePreview
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import org.koin.androidx.compose.getViewModel
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun SessionDetailsRoute(
    columnState: ScalingLazyColumnState,
    viewModel: SessionDetailsViewModel = getViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val timeZone = remember { viewModel.timeZone }
    SessionDetailView(
        session = session,
        columnState = columnState,
        formatter = { viewModel.formatter.format(it, timeZone, "eeee HH:mm")})
}

@Composable
fun SessionDetailView(
    session: SessionDetails?,
    columnState: ScalingLazyColumnState,
    formatter: (Instant) -> String
) {
    val description = session.descriptionParagraphs()

    ScalingLazyColumn(columnState = columnState) {
        session?.let { session ->
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                ) {
                    Text(
                        text = session.title,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.title3
                    )
                }
            }

            item {
                val time = remember(session) {
                    formatter(session.startInstant)
                }
                Text(time)
            }

            items(description) {
                Text(text = it)
            }

            items(session.speakers) { speaker ->
                SessionSpeakerInfo(speaker = speaker.speakerDetails)
            }
        }
    }
}

private fun SessionDetails?.descriptionParagraphs(): List<String> =
    this?.sessionDescription?.split("\n+".toRegex()).orEmpty()

@WearSmallRoundDevicePreview
@Composable
fun SessionDetailsLongText() {
    val sessionTime = LocalDateTime.of(2022, 12, 25, 12, 30)
    val startInstant = sessionTime.toInstant(ZoneOffset.UTC).toKotlinInstant()

    ConfettiTheme {
        SessionDetailView(
            session = SessionDetails(
                "1",
                "This is a really long talk title that seems to go forever.",
                "Talk",
                startInstant,
                startInstant,
                "Be aWear of what's coming, don't walk, run to attend this session.",
                "en",
                listOf(),
                SessionDetails.Room("Main Hall"),
                listOf()
            ),
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            formatter = { AndroidDateService().format(it, TimeZone.UTC, "eeee HH:mm") }
        )
    }
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun SessionDetailsViewPreview() {
    val sessionTime = LocalDateTime.of(2022, 12, 25, 12, 30)
    val startInstant = sessionTime.toInstant(ZoneOffset.UTC).toKotlinInstant()

    ConfettiTheme {
        SessionDetailView(
            session = SessionDetails(
                "1",
                "Wear it's at",
                "Talk",
                startInstant,
                startInstant,
                "Be aWear of what's coming",
                "en",
                listOf(),
                SessionDetails.Room("Main Hall"),
                listOf()
            ),
            columnState = ScalingLazyColumnDefaults.belowTimeText().create(),
            formatter = { AndroidDateService().format(it, TimeZone.UTC, "eeee HH:mm") }
        )
    }
}

