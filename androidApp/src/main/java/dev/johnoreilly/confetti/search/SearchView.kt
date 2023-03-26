@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)

package dev.johnoreilly.confetti.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MicExternalOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.sessiondetails.navigation.SessionDetailsKey
import dev.johnoreilly.confetti.sessions.SessionItemView
import dev.johnoreilly.confetti.speakerdetails.navigation.SpeakerDetailsKey
import dev.johnoreilly.confetti.speakers.SpeakerItemView
import dev.johnoreilly.confetti.ui.ConfettiAppState
import dev.johnoreilly.confetti.ui.ConfettiScaffold
import dev.johnoreilly.confetti.ui.ConfettiTypography
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.component.ConfettiBackground
import dev.johnoreilly.confetti.utils.rememberRunnable

@Composable
fun SearchView(
    conference: String,
    appState: ConfettiAppState,
    search: String,
    onSearchChange: (String) -> Unit,
    sessions: List<SessionDetails>,
    speakers: List<SpeakerDetails>,
    navigateToSession: (SessionDetailsKey) -> Unit,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    loading: Boolean,
) {
    ConfettiScaffold(
        title = stringResource(R.string.search),
        conference = conference,
        appState = appState,
        onSwitchConference = onSwitchConference,
        onSignIn = onSignIn,
        onSignOut = onSignOut,
    ) {
        Column {
            SearchTextField(
                modifier = Modifier
                    .padding(8.dp),
                value = search,
                onValueChange = onSearchChange,
            )

            if (loading) {
                LoadingView()
            } else if (search.isNotBlank()) {
                SearchContent(
                    sessionsListContent = {
                        sessionItems(
                            conference = conference,
                            sessions = sessions,
                            navigateToSession = navigateToSession,
                            bookmarks = bookmarks,
                            addBookmark = addBookmark,
                            removeBookmark = removeBookmark,
                            onSignIn = onSignIn,
                        )
                    },
                    speakersListContent = {
                        speakerItems(
                            conference = conference,
                            speakers = speakers,
                            navigateToSpeaker = navigateToSpeaker,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchContent(
    sessionsListContent: LazyListScope.() -> Unit,
    speakersListContent: LazyListScope.() -> Unit,
) {
    LazyColumn {
        sessionsListContent()
        speakersListContent()
    }
}

private fun LazyListScope.speakerItems(
    conference: String,
    speakers: List<SpeakerDetails>,
    navigateToSpeaker: (SpeakerDetailsKey) -> Unit,
) {
    // Shows header if and only if there are speaker results.
    if (speakers.isNotEmpty()) {
        stickyHeader {
            HeaderView(Icons.Filled.Person, "Speakers")
        }
    }
    items(speakers) { speaker ->
        SpeakerItemView(
            conference = conference,
            speaker = speaker,
            navigateToSpeaker = navigateToSpeaker,
        )
    }
}

private fun LazyListScope.sessionItems(
    conference: String,
    sessions: List<SessionDetails>,
    navigateToSession: (SessionDetailsKey) -> Unit,
    bookmarks: Set<String>,
    addBookmark: (sessionId: String) -> Unit,
    removeBookmark: (sessionId: String) -> Unit,
    onSignIn: () -> Unit,
) {
    // Shows header if and only if there are session results.
    if (sessions.isNotEmpty()) {
        stickyHeader {
            HeaderView(Icons.Filled.MicExternalOn, "Sessions")
        }
    }
    items(sessions) { session ->
        SessionItemView(
            conference = conference,
            session = session,
            sessionSelected = navigateToSession,
            isBookmarked = bookmarks.contains(session.id),
            addBookmark = { sessionId -> addBookmark(sessionId) },
            removeBookmark = { sessionId -> removeBookmark(sessionId) },
            onNavigateToSignIn = onSignIn,
        )
    }
}

@Composable
private fun HeaderView(
    icon: ImageVector,
    text: String,
) {
    ConfettiBackground(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier
                        .padding(end = 8.dp),
                    imageVector = icon,
                    contentDescription = null,
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Divider()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: (String) -> Unit,
    onCloseSearch: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val closeSearch = rememberRunnable {
        keyboardController?.hide()
        onValueChange("")
        onCloseSearch()
    }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { keyboardController?.hide() }
    }

    TextField(
        modifier = modifier
            .focusRequester(focusRequester)
            .interceptKey(Key.Enter) {
                keyboardController?.hide()
            }
            .fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("What are you looking for?") },
        leadingIcon = { Icon(Icons.Filled.Search, "Search") },
        trailingIcon = {
            if (value.isNotBlank()) {
                IconButton(onClick = { closeSearch() }) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close Search"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { keyboardController?.hide() }
        ),
        colors = TextFieldDefaults.textFieldColors(
            // hide the indicator
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        textStyle = ConfettiTypography.bodyLarge,
        shape = ShapeDefaults.Large,
        singleLine = true,
    )
}

/**
 * [Modifier] to intercept [key] events and fires [onKeyEvent] callback when the key is released.
 *
 * The [key] parameter represents the key to be intercepted
 * The [onKeyEvent] listener is an optional listener to when the key event happens.
 *
 * The intercepted key is not passed to any child composable.
 */
fun Modifier.interceptKey(key: Key, onKeyEvent: () -> Unit = {}): Modifier =
    onPreviewKeyEvent { event ->
        if (event.key == key && event.type == KeyEventType.KeyUp) {
            onKeyEvent()
        }
        event.key == key
    }
