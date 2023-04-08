@file:OptIn(ExperimentalMaterial3Api::class)

package dev.johnoreilly.confetti.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.confetti.account.AccountIcon
import dev.johnoreilly.confetti.account.WearUiState
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.settings.navigation.SettingsKey
import dev.johnoreilly.confetti.wear.WearSettingsSync
import org.koin.compose.koinInject

class ScaffoldState(
    val title: MutableState<String?>,
    val snackbarHostState: SnackbarHostState,
    val user: User?
)

/**
 * A wrapper for some content view that handles the different layouts (mobile/tablet, etc...)
 */
@Composable
fun ConfettiScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    conference: String,
    appState: ConfettiAppState,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    content: @Composable (ScaffoldState) -> Unit,
) {
    val authentication = koinInject<Authentication>()
    val wearSettingSync = koinInject<WearSettingsSync>()
    fun signOut() {
        authentication.signOut()
        onSignOut()
    }

    val wearSettingNodeState =
        wearSettingSync.wearNodes.collectAsStateWithLifecycle(emptyList()).value
    val wearSettingsUIState = WearUiState(wearSettingNodeState)

    fun installOnWear() {
        wearSettingNodeState.filter { !it.isAppInstalled }.forEach {
            wearSettingSync.installOnWearNode(it.id)
        }
    }

    val user by authentication.currentUser.collectAsStateWithLifecycle()

    val titleState = remember(title) { mutableStateOf(title) }

    ConfettiScaffold(
        modifier = modifier,
        title = titleState.value,
        conference = conference,
        appState = appState,
        onSwitchConference = onSwitchConference,
        onSignIn = onSignIn,
        onSignOut = ::signOut,
        user = user,
        installOnWear = ::installOnWear,
        wearSettingsUiState = wearSettingsUIState,
    ) {
        content(ScaffoldState(titleState, it, user))
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfettiScaffold(
    modifier: Modifier = Modifier,
    title: String?,
    conference: String,
    appState: ConfettiAppState,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    user: User?,
    installOnWear: () -> Unit,
    wearSettingsUiState: WearUiState,
    content: @Composable (SnackbarHostState) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val titleFontSize =
        if (appState.isExpandedScreen) 40.sp else MaterialTheme.typography.titleLarge.fontSize

    Row(modifier = modifier) {
        // The default behaviour is to keep top bar always visible.
        val scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        if (appState.shouldShowNavRail) {
            ConfettiNavRail(
                conference = conference,
                onNavigateToDestination = appState::navigateToTopLevelDestination,
                currentDestination = appState.currentDestination,
                modifier = Modifier.safeDrawingPadding()
            )
        } else {
            // The scrollBehaviour combined with a pager seems to cause issues when a fling happens.
            // For now, we will keep the default behaviour until we figure out how to workaround it.
            // ---
            // If NavRail is not visible (i.e., phones), we collapse the top bar while scrolling.
            // That gives more space for the user to see the screen content.
            // scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        }
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        if (title != null) {
                            Text(text = title, fontSize = titleFontSize)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        AccountIcon(
                            onSwitchConference = onSwitchConference,
                            onSignIn = onSignIn,
                            onSignOut = onSignOut,
                            onShowSettings = { appState.navigate(SettingsKey.route) },
                            user = user,
                            installOnWear = installOnWear,
                            wearSettingsUiState = wearSettingsUiState,
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )

            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxHeight()
            ) {
                val shouldShowBottomBar = appState.shouldShowBottomBar
                // If the bottom bar is shown, we must properly inform our content() that the
                // navigation bar insets have been consumed already.
                val consumedInsetsModifier = if (shouldShowBottomBar) {
                    Modifier.consumeWindowInsets(NavigationBarDefaults.windowInsets)
                } else {
                    Modifier
                }
                Box(modifier = Modifier
                    .weight(1f)
                    .then(consumedInsetsModifier)) {
                    content(snackbarHostState)
                }
                if (shouldShowBottomBar) {
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
