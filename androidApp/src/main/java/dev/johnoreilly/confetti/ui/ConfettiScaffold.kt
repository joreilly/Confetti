@file:OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)

package dev.johnoreilly.confetti.ui

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
                            onShowSettings = { appState.setShowSettingsDialog(true) },
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )

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
