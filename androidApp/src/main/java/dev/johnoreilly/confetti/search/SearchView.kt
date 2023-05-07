@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class, ExperimentalLayoutApi::class
)

package dev.johnoreilly.confetti.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import dev.johnoreilly.confetti.sessions.SessionItemView
import dev.johnoreilly.confetti.speakers.SpeakerItemView
import dev.johnoreilly.confetti.ui.ConfettiTypography
import dev.johnoreilly.confetti.ui.LoadingView
import dev.johnoreilly.confetti.ui.component.ConfettiHeader
import dev.johnoreilly.confetti.utils.rememberRunnable

@Composable
fun SearchView(
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
            LazyColumn(
                modifier = Modifier.imeNestedScroll(),
                contentPadding = WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Bottom)
                    .asPaddingValues()
            ) {
                sessionItems(
                    sessions = sessions,
                    navigateToSession = navigateToSession,
                    bookmarks = bookmarks,
                    addBookmark = addBookmark,
                    removeBookmark = removeBookmark,
                    onSignIn = onSignIn,
                    isLoggedIn = isLoggedIn,
                )
                speakerItems(
                    speakers = speakers,
                    navigateToSpeaker = navigateToSpeaker,
                )
            }
        }
    }
}

private fun LazyListScope.speakerItems(
    speakers: List<SpeakerDetails>,
    navigateToSpeaker: (id: String) -> Unit,
) {
    // Shows header if and only if there are speaker results.
    if (speakers.isNotEmpty()) {
        stickyHeader {
            ConfettiHeader(
                icon = Icons.Filled.Person,
                text = stringResource(R.string.speakers),
            )
        }
    }
    items(speakers) { speaker ->
        SpeakerItemView(
            speaker = speaker,
            navigateToSpeaker = navigateToSpeaker,
        )
    }
}

private fun LazyListScope.sessionItems(
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
            ConfettiHeader(
                icon = Icons.Filled.Event,
                text = stringResource(R.string.sessions),
            )
        }
    }
    items(sessions) { session ->
        SessionItemView(
            session = session,
            sessionSelected = navigateToSession,
            isBookmarked = bookmarks.contains(session.id),
            addBookmark = { sessionId -> addBookmark(sessionId) },
            removeBookmark = { sessionId -> removeBookmark(sessionId) },
            onNavigateToSignIn = onSignIn,
            isLoggedIn = isLoggedIn,
        )
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
        placeholder = { Text(stringResource(id = R.string.search_placeholder)) },
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
