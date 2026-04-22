package dev.johnoreilly.confetti.wear.sessiondetails

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalScrollCaptureInProgress
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
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
    navigateToSpeaker: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("eeee HH:mm") }
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(
        modifier = modifier,
        scrollState = columnState,
        scrollIndicator = {
            if (!LocalScrollCaptureInProgress.current) {
                ScrollIndicator(columnState)
            }
        },
    ) { contentPadding ->
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = contentPadding,
        ) {
            when (uiState) {
                is SessionDetailsUiState.Success -> {
                    val session = uiState.sessionDetails
                    val description = session.descriptionParagraphs()

                    item {
                        ScreenHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            text = session.title,
                            transformation = SurfaceTransformation(transformationSpec),
                        )
                    }

                    item {
                        val time = remember(session.startsAt) {
                            timeFormatter.format(session.startsAt.toJavaLocalDateTime())
                        }
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    with(transformationSpec) {
                                        applyContainerTransformation(scrollProgress)
                                    }
                                }
                                .transformedHeight(this, transformationSpec),
                            text = time,
                        )
                    }

                    items(description) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    with(transformationSpec) {
                                        applyContainerTransformation(scrollProgress)
                                    }
                                }
                                .transformedHeight(this, transformationSpec),
                            text = it,
                        )
                    }

                    items(session.speakers) { speaker ->
                        SessionSpeakerChip(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec)
                                .minimumVerticalContentPadding(
                                    ButtonDefaults.minimumVerticalListContentPadding
                                ),
                            speaker = speaker.speakerDetails,
                            navigateToSpeaker = navigateToSpeaker,
                            transformation = SurfaceTransformation(transformationSpec),
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
