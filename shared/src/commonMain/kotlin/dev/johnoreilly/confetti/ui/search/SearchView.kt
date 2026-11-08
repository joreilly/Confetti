@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class, ExperimentalLayoutApi::class
)

package dev.johnoreilly.confetti.ui.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.search_placeholder
import confetti.shared.generated.resources.sessions
import confetti.shared.generated.resources.speakers
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.preview.MobilePreviews
import dev.johnoreilly.confetti.preview.lightningSession
import dev.johnoreilly.confetti.preview.sampleSpeakers
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.ui.component.ConfettiSearch
import dev.johnoreilly.confetti.ui.component.LoadingView
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import dev.johnoreilly.confetti.ui.components.SessionCardModel
import dev.johnoreilly.confetti.preview.ComponentsPreview
import dev.johnoreilly.confetti.listTags
import dev.johnoreilly.confetti.ui.speakers.SpeakerItemView
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.PaddingValues
import dev.johnoreilly.confetti.ui.LocalBottomNavigationPadding

import dev.johnoreilly.confetti.isBreak
import dev.johnoreilly.confetti.isService
import dev.johnoreilly.confetti.ui.component.EmptyView

@Composable
context(components: ConferenceComponents)
fun SearchView(
    conference: String,
    search: String,
    onSearchChange: (String) -> Unit,
    sessions: List<SessionDetails>,
    speakers: List<SpeakerDetails>,
    navigateToSession: (id: String) -> Unit,
    navigateToSpeaker: (id: String) -> Unit,
    onSignIn: () -> Unit,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    loading: Boolean,
    isLoggedIn: Boolean,
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            ConfettiSearch(
                value = search,
                onValueChange = onSearchChange,
                onBackClick = onBackClick,
                placeholder = stringResource(Res.string.search_placeholder)
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .imePadding()
        ) {
            if (loading) {
                LoadingView()
            } else if (search.isNotBlank()) {
                if (sessions.isEmpty() && speakers.isEmpty()) {
                    EmptyView(
                        text = "No results found",
                        message = "No sessions or speakers match \"$search\"",
                        icon = Icons.Default.Search,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp + navigationBarsPadding)
                    ) {
                        sessionItems(
                            conference = conference,
                            sessions = sessions,
                            navigateToSession = navigateToSession,
                            bookmarks = bookmarks,
                            addBookmark = addBookmark,
                            removeBookmark = removeBookmark,
                            onSignIn = onSignIn,
                            isLoggedIn = isLoggedIn,
                        )
                        speakerItems(
                            conference = conference,
                            speakers = speakers,
                            navigateToSpeaker = navigateToSpeaker,
                        )
                    }
                }
            } else {
                EmptyView(
                    text = "Search Confetti",
                    message = "Find sessions, speakers, and topics",
                    icon = Icons.Default.Search,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
context(components: ConferenceComponents)
private fun LazyListScope.speakerItems(
    conference: String,
    speakers: List<SpeakerDetails>,
    navigateToSpeaker: (id: String) -> Unit,
) {
    // Shows header if and only if there are speaker results.
    if (speakers.isNotEmpty()) {
        stickyHeader {
            components.SectionHeader(conference, stringResource(Res.string.speakers), Icons.Filled.Person)
        }
    }
    items(speakers) { speaker ->
        SpeakerItemView(
            speaker = speaker,
            navigateToSpeaker = navigateToSpeaker,
        )
    }
}

context(components: ConferenceComponents)
private fun LazyListScope.sessionItems(
    conference: String,
    sessions: List<SessionDetails>,
    navigateToSession: (id: String) -> Unit,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onSignIn: () -> Unit,
    isLoggedIn: Boolean,
) {
    // Shows header if and only if there are session results.
    if (sessions.isNotEmpty()) {
        stickyHeader {
            components.SectionHeader(conference, stringResource(Res.string.sessions), Icons.Filled.Event)
        }
    }
    itemsIndexed(sessions) { index, session ->
        components.SessionCard(
            SessionCardModel(
                conference = conference,
                session = session,
                now = null,
                isBookmarked = bookmarks.contains(session.id),
                isLoggedIn = isLoggedIn,
                trackColor = null,
                tags = session.listTags(),
                sessionSelected = navigateToSession,
                addBookmark = addBookmark,
                removeBookmark = removeBookmark,
                onNavigateToSignIn = onSignIn,
            )
        )
        val isCurrentBreak = session.isBreak() || session.isService()
        val isNextBreak = index < sessions.lastIndex && (sessions[index + 1].isBreak() || sessions[index + 1].isService())
        if (index < sessions.lastIndex && !isCurrentBreak && !isNextBreak) {
            HorizontalDivider()
        }
    }
}



@MobilePreviews
@Composable
internal fun SearchViewLoadedPreview() {
    ComponentsPreview {
        SearchView(
            conference = "kotlinconf2023",
            search = "kotlin",
            onSearchChange = {},
            sessions = listOf(sessionDetails, lightningSession),
            speakers = sampleSpeakers,
            navigateToSession = {},
            navigateToSpeaker = {},
            onSignIn = {},
            bookmarks = setOf(sessionDetails.id),
            addBookmark = {},
            removeBookmark = {},
            loading = false,
            isLoggedIn = true,
        )
    }
}

@Preview(name = "Empty query", widthDp = 411, heightDp = 914, showBackground = true)
@Composable
internal fun SearchViewEmptyQueryPreview() {
    ComponentsPreview {
        SearchView(
            conference = "kotlinconf2023",
            search = "",
            onSearchChange = {},
            sessions = emptyList(),
            speakers = emptyList(),
            navigateToSession = {},
            navigateToSpeaker = {},
            onSignIn = {},
            bookmarks = emptySet(),
            addBookmark = {},
            removeBookmark = {},
            loading = false,
            isLoggedIn = false,
        )
    }
}

@Preview(name = "Loading", widthDp = 411, heightDp = 914, showBackground = true)
@Composable
internal fun SearchViewLoadingPreview() {
    ComponentsPreview {
        SearchView(
            conference = "kotlinconf2023",
            search = "kotlin",
            onSearchChange = {},
            sessions = emptyList(),
            speakers = emptyList(),
            navigateToSession = {},
            navigateToSpeaker = {},
            onSignIn = {},
            bookmarks = emptySet(),
            addBookmark = {},
            removeBookmark = {},
            loading = true,
            isLoggedIn = false,
        )
    }
}
