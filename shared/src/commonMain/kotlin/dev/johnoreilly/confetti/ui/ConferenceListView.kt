@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import conferenceDayMonthFormat
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.preview.previewConferenceListState
import dev.johnoreilly.confetti.preview.sampleConferences
import dev.johnoreilly.confetti.ui.component.ConfettiSearch
import dev.johnoreilly.confetti.ui.component.LoadingView
import kotlinx.datetime.LocalDate



@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConferenceListView(component: ConferencesComponent) {
    var searchQuery by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(searchMode) {
        if (searchMode) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            if (searchMode) {
                ConfettiSearch(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    onBackClick = {
                        searchMode = false
                        searchQuery = ""
                    },
                    placeholder = "Search conferences..."
                )
            } else {
                CenterAlignedTopAppBar(
                    title = { Text("Confetti") },
                    navigationIcon = {
                        component.onBack?.let { onBack ->
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { searchMode = true }) {
                            Icon(Icons.Outlined.Search, contentDescription = "Search")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->

        val uiState by component.uiState.subscribeAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val uiState1 = uiState) {
                ConferencesComponent.Error -> {} //ErrorView(component::refresh)
                ConferencesComponent.Loading -> LoadingView()

                is ConferencesComponent.Success -> {
                    val filteredConferenceListByYear = if (searchQuery.isBlank()) {
                        uiState1.conferenceListByYear
                    } else {
                        uiState1.conferenceListByYear.mapValues { (year, list) ->
                            list.filter { conference ->
                                conference.name.contains(searchQuery, ignoreCase = true) ||
                                    conference.timezone.contains(searchQuery, ignoreCase = true) ||
                                    year.toString().contains(searchQuery)
                            }
                        }.filterValues { it.isNotEmpty() }
                    }

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        filteredConferenceListByYear.keys.sortedDescending().forEach { year ->
                            val conferenceList = filteredConferenceListByYear[year]
                            conferenceList?.let {
                                stickyHeader {
                                    YearHeader(year.toString())
                                }

                                items(conferenceList) { conference ->
                                    val isSelected = conference.id == uiState1.currentConference
                                    ConferenceCard(
                                        conference = conference,
                                        isSelected = isSelected,
                                        navigateToConference = {
                                            component.onConferenceClicked(conference)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ConferenceCard(
    conference: GetConferencesQuery.Conference,
    isSelected: Boolean = false,
    navigateToConference: (GetConferencesQuery.Conference) -> Unit
) {
    ConferenceMaterialThemeFromSettings(conference.themeColor) {
        Card(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = {
                    navigateToConference(conference)
                })
                .fillMaxWidth()
                .testTag(conference.id),
            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conference.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = "Selected",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Event,
                        contentDescription = "Event dates",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getConferenceDatesString(conference.days),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


private fun getConferenceDatesString(days: List<LocalDate>): String {
    var conferenceDatesString = ""
    if (days.isNotEmpty()) {
        conferenceDatesString = days[0].conferenceDayMonthFormat()
    }
    if (days.size == 2) {
        conferenceDatesString += " - ${days[1].conferenceDayMonthFormat()}"
    }
    return conferenceDatesString
}

@Composable
fun YearHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

private class PreviewConferencesComponent(
    state: ConferencesComponent.UiState,
) : ConferencesComponent {
    override val uiState: Value<ConferencesComponent.UiState> = MutableValue(state)
    override val onBack: (() -> Unit)? = null
    override fun refresh() {}
    override fun onConferenceClicked(conference: GetConferencesQuery.Conference) {}
}

@MobilePreviews
@Composable
internal fun ConferenceListViewLoadedPreview() {
    ConferenceListView(
        component = PreviewConferencesComponent(previewConferenceListState),
    )
}

@Preview(name = "Loading", widthDp = 411, heightDp = 914, showBackground = true)
@Composable
internal fun ConferenceListViewLoadingPreview() {
    ConferenceListView(
        component = PreviewConferencesComponent(ConferencesComponent.Loading),
    )
}

@Preview(name = "Conference card", widthDp = 411, heightDp = 140, showBackground = true)
@Composable
internal fun ConferenceCardPreview() {
    ConferenceCard(
        conference = sampleConferences.first(),
        navigateToConference = {},
    )
}
