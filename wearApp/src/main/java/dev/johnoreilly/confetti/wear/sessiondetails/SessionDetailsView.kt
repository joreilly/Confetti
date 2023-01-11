@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.sessiondetails

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import kotlinx.datetime.toKotlinInstant
import org.koin.androidx.compose.getViewModel
import java.time.LocalDateTime
import java.time.ZoneOffset

@Composable
fun SessionDetailsRoute(
    columnState: ScalingLazyColumnState,
    onBackClick: () -> Unit,
    viewModel: SessionDetailsViewModel = getViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    SessionDetailView(session, columnState, onBackClick)
}

@Composable
fun SessionDetailView(
    session: SessionDetails?,
    columnState: ScalingLazyColumnState,
    popBack: () -> Unit
) {
    val context = LocalContext.current

    ScalingLazyColumn(columnState = columnState) {
        session?.let { session ->
            item {
                ListHeader {
                    Text(
                        text = session.title,
                    )
                }
            }

            session.description?.let {
                item {
                    Text(text = it)
                }
            }

//            if (session.tags.isNotEmpty()) {
//                Spacer(modifier = Modifier.size(16.dp))
//                    FlowRow(crossAxisSpacing = 8.dp) {
//                        session.tags.forEach { tag ->
//                            Chip(tag)
//                        }
//                    }
//            }

            items(session.speakers) { speaker ->
                SessionSpeakerInfo(speaker = speaker.speakerDetails,
                    onSocialLinkClick = { socialItem, _ ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(socialItem.link))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
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
            popBack = {},
            columnState = ScalingLazyColumnDefaults.belowTimeText().create()
        )
    }
}

