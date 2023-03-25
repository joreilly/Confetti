@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
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
            SearchTextField(value = search, onValueChange = onSearchChange)

            LazyColumn {
                if (sessions != null) {
                    stickyHeader {
                        Text(text = "Sessions", style = TextStyle(fontSize = 16.sp))
                    }
                }
                items(sessions) { session ->
                    SessionItemView(
                        conference = conference,
                        session = session,
                        sessionSelected = navigateToSession,
                    )
                }

                if (speakers != null) {
                    stickyHeader {
                        Text(text = "Speakers", style = TextStyle(fontSize = 16.sp))
                    }
                }
                items(speakers) { speaker ->
                    SpeakerItemView(
                        conference = conference,
                        speaker = speaker,
                        navigateToSpeaker = navigateToSpeaker
                    )
                }
            }
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

    DisposableEffect(Unit) {
//        focusRequester.requestFocus()
        onDispose { onValueChange("") }
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
        placeholder = { Text("Search") },
        leadingIcon = { Icon(Icons.Filled.Search, "Search") },
        trailingIcon = {
            IconButton(onClick = { onCloseSearch() }) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close Search"
                )
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
