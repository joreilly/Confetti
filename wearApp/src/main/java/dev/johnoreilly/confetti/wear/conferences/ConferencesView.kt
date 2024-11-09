package dev.johnoreilly.confetti.wear.conferences

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.scrollTransform
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.wear.components.PlaceholderButton
import dev.johnoreilly.confetti.wear.components.ScreenHeader
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.toColor

@Composable
fun ConferencesRoute(
    component: ConferencesComponent,
) {
    val uiState by component.uiState.subscribeAsState()

    if (!BuildConfig.DEBUG) {
        ReportDrawnWhen {
            uiState !is ConferencesComponent.Loading
        }
    }

    ConferencesView(
        uiState = uiState,
        navigateToConference = { conference ->
            component.onConferenceClicked(conference)
        }
    )
}

@Composable
fun ConferencesView(
    uiState: ConferencesComponent.UiState,
    navigateToConference: (GetConferencesQuery.Conference) -> Unit,
    modifier: Modifier = Modifier
) {
    val columnState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = columnState) {
        TransformingLazyColumn(
            modifier = modifier.fillMaxSize(), state = columnState,
            contentPadding = rememberResponsiveColumnPadding(
                first = ColumnItemType.ListHeader,
                last = ItemType.Chip
            ),
        ) {
            item {
                ScreenHeader(
                    modifier = Modifier
                        .scrollTransform(this@item), text = "Conferences"
                )
            }

            when (uiState) {
                is ConferencesComponent.Loading -> {
                    items(5) {
                        PlaceholderButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scrollTransform(this@items),
                        )
                    }
                }

                is ConferencesComponent.Success -> {
                    // TODO show current year
                    items(uiState.relevantConferences) { conference ->
                        ConferencesChip(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scrollTransform(this@items),
                            conference,
                            navigateToConference,
                        )
                    }
                }

                else -> {
                    // TODO
                }
            }
        }
    }
}

@Composable
private fun ConferencesChip(
    modifier: Modifier = Modifier,
    conference: GetConferencesQuery.Conference,
    navigateToConference: (GetConferencesQuery.Conference) -> Unit
) {
    val seedColor = conference.themeColor?.toColor()

    ConfettiTheme(seedColor = seedColor) {
        Button(
            modifier = modifier.fillMaxWidth(),
            onClick = {
                navigateToConference(conference)
            },
            colors = ButtonDefaults.filledVariantButtonColors()
        ) { Text(conference.name) }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun ConferencesViewPreview() {
    ConferencesView(
        uiState = ConferencesComponent.Success(
            TestFixtures.conferences.groupBy { it.days[0].year }
        ),
        navigateToConference = {},
    )
}
