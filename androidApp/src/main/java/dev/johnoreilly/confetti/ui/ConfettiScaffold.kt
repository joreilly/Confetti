package dev.johnoreilly.confetti.ui

import android.annotation.SuppressLint
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import dev.johnoreilly.confetti.account.AccountIcon

/**
 * A wrapper for some content view that handles the different layouts (mobile/tablet, etc...)
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfettiScaffold(
    title: String?,
    appState: ConfettiAppState,
    onSwitchConference: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    content: @Composable () -> Unit,
) {
    val titleFontSize =
        if (appState.isExpandedScreen) 40.sp else MaterialTheme.typography.titleLarge.fontSize
    Row {
        if (appState.shouldShowNavRail) {
            ConfettiNavRail(
                destinations = appState.topLevelDestinations,
                onNavigateToDestination = appState::navigate,
                currentDestination = appState.currentDestination,
                modifier = Modifier.safeDrawingPadding()
            )
        }
        Scaffold(
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
                        )
                    }
                )
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            Column(modifier = Modifier.padding(it).fillMaxHeight()) {
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
                if (appState.shouldShowBottomBar) {
                    ConfettiBottomBar(
                        destinations = appState.topLevelDestinations,
                        onNavigateToDestination = appState::navigate,
                        currentDestination = appState.currentDestination
                    )
                }
            }
        }
    }
}