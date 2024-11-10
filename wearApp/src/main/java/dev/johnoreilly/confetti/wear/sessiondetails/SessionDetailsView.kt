package dev.johnoreilly.confetti.wear.sessiondetails

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
//import androidx.wear.compose.material3.lazy.scrollTransform
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding
import dev.johnoreilly.confetti.decompose.SessionDetailsUiState
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.components.ScreenHeader
import dev.johnoreilly.confetti.wear.components.SessionSpeakerChip
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SessionDetailView(
    uiState: SessionDetailsUiState,
    columnState: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
    navigateToSpeaker: (String) -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("eeee HH:mm") }

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = rememberResponsiveColumnPadding(
                first = ColumnItemType.ListHeader,
                last = ColumnItemType.Button
            ),
        ) {
            when (uiState) {
                is SessionDetailsUiState.Success -> {
                    val session = uiState.sessionDetails
                    val description = session.descriptionParagraphs()

                    item {
                        ScreenHeader(
                            modifier = Modifier,
//                                .scrollTransform(this@item),
                            text = session.title
                        )
                    }

                    item {
                        val time = remember(session.startsAt) {
                            timeFormatter.format(session.startsAt.toJavaLocalDateTime())
                        }
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(),
//                                .scrollTransform(this@item),
                            text = time,
                        )
                    }

                    items(description) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(),
//                                .scrollTransform(this@items),
                            text = it,
                        )
                    }

                    items(session.speakers) { speaker ->
                        SessionSpeakerChip(
                            modifier = Modifier
                                .fillMaxWidth(),
//                                .scrollTransform(this@items),
                            speaker = speaker.speakerDetails,
                            navigateToSpeaker = navigateToSpeaker
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

private fun SessionDetails?.descriptionParagraphs(): List<String> =
    this?.sessionDescription?.split("\n+".toRegex()).orEmpty()
