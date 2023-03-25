@file:OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)

package dev.johnoreilly.confetti.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import dev.johnoreilly.confetti.account.AccountIcon
import dev.johnoreilly.confetti.settings.SettingsDialog

/**
 * A wrapper for some content view that handles the different layouts (mobile/tablet, etc...)
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfettiScaffold(
    title: String?,
    conference: String,
    appState: ConfettiAppState,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    search: String? = null,
    onSearch: (String) -> Unit = {},
    content: @Composable (SnackbarHostState) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val titleFontSize =
        if (appState.isExpandedScreen) 40.sp else MaterialTheme.typography.titleLarge.fontSize

    if (appState.shouldShowSettingsDialog) {
        SettingsDialog(
            onDismiss = { appState.setShowSettingsDialog(false) },
        )
    }

    Row {
        // The default behaviour is to keep top bar always visible.
        var scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        if (appState.shouldShowNavRail) {
            ConfettiNavRail(
                conference = conference,
                onNavigateToDestination = appState::navigateToTopLevelDestination,
                currentDestination = appState.currentDestination,
                modifier = Modifier.safeDrawingPadding()
            )
        } else {
            // If NavRail is not visible (i.e., phones), we collapse the top bar while scrolling.
            // That gives more space for the user to see the screen content.
            scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        }
        var isSearching by remember { mutableStateOf(false) }
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                // Animates the transition when starting a search.
                AnimatedContent(targetState = isSearching, label = "search") { shouldShowSearch ->
                    if (shouldShowSearch) {
                        // Wraps search in a top-bar to maintain same appearance and sizing.
                        CenterAlignedTopAppBar(
                            title = {
                                SearchTextField(
                                    value = search.orEmpty(),
                                    onValueChange = onSearch,
                                    onCloseSearch = {
                                        isSearching = false
                                        onSearch("")
                                    }
                                )
                            }
                        )
                    } else {
                        CenterAlignedTopAppBar(
                            title = {
                                if (title != null) {
                                    Text(text = title, fontSize = titleFontSize)
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent
                            ),
                            navigationIcon = {
                                AccountIcon(
                                    onSwitchConference = onSwitchConference,
                                    onSignIn = onSignIn,
                                    onSignOut = onSignOut,
                                    onShowSettings = { appState.setShowSettingsDialog(true) },
                                )
                            },
                            actions = {
                                IconButton(onClick = { isSearching = true }) {
                                    Icon(Icons.Filled.Search, contentDescription = "Search")
                                }
                            },
                            scrollBehavior = scrollBehavior,
                        )
                    }
                }

            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxHeight()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    content(snackbarHostState)
                }
                if (appState.shouldShowBottomBar) {
                    ConfettiBottomBar(
                        conference = conference,
                        onNavigateToDestination = appState::navigateToTopLevelDestination,
                        currentDestination = appState.currentDestination
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchTextField(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
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
            focusedIndicatorColor = Color.Transparent, //hide the indicator
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
        if (event.key == key && event.type == KeyUp) {
            onKeyEvent()
        }
        event.key == key
    }
