@file:OptIn(ExperimentalHorologistComposeLayoutApi::class)

package dev.johnoreilly.confetti.wear.conferences

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.items
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.scrollAway
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.wear.ui.ConfettiTheme
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import org.koin.androidx.compose.getViewModel

@Composable
fun ConferencesRoute(
    navigateToConference: (String) -> Unit,
    columnState: ScalingLazyColumnState,
    viewModel: ConfettiViewModel = getViewModel()
) {
    val conferenceList by viewModel.conferenceList.collectAsStateWithLifecycle()

    Scaffold(
        timeText = { TimeText(modifier = Modifier.scrollAway(columnState)) },
        positionIndicator = { PositionIndicator(columnState.state) }
    ) {
        ConferencesView(conferenceList = conferenceList,
            columnState = columnState,
            navigateToConference = { conference ->
                viewModel.setConference(conference)
                navigateToConference(conference)
            }
        )
    }
}

@Composable
fun ConferencesView(
    conferenceList: List<GetConferencesQuery.Conference>,
    navigateToConference: (String) -> Unit,
    columnState: ScalingLazyColumnState,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(), columnState = columnState
    ) {
        item {
            ListHeader {
                Text("Conferences")
            }
        }

        if (conferenceList.isEmpty()) {
            item {
                CircularProgressIndicator(modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center))
            }
        } else {
            items(conferenceList) { conference ->
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(conference.name) },
                    onClick = {
                        navigateToConference(conference.id)
                    },
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun ConferencesViewPreview() {
    ConfettiTheme {
        ConferencesView(
            conferenceList = listOf(
                GetConferencesQuery.Conference("0", emptyList(), "Droidcon San Francisco 2022"),
                GetConferencesQuery.Conference("1", emptyList(), "FrenchKit 2022"),
                GetConferencesQuery.Conference("2", emptyList(), "Droidcon London 2022"),
                GetConferencesQuery.Conference("3", emptyList(), "DevFest Ukraine 2023"),
            ),
            navigateToConference = {},
            columnState = ScalingLazyColumnDefaults.
                belowTimeText().create()
        )
    }
}
