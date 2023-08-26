package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import dev.johnoreilly.confetti.decompose.HomeComponent
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.account.AccountIcon
import dev.johnoreilly.confetti.account.AccountInfo
import dev.johnoreilly.confetti.account.WearUiState
import dev.johnoreilly.confetti.bookmarks.BookmarksRoute
import dev.johnoreilly.confetti.search.SearchRoute
import dev.johnoreilly.confetti.sessions.SessionsRoute
import dev.johnoreilly.confetti.speakers.SpeakersRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    component: HomeComponent,
    windowSizeClass: WindowSizeClass,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val shouldShowNavRail = windowSizeClass.isExpanded

    Row {
        if (shouldShowNavRail) {
            NavigationRail(component = component)
        }

        Scaffold(
            modifier = Modifier.imePadding(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxHeight(),
            ) {
                Children(
                    component = component,
                    windowSizeClass = windowSizeClass,
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier
                        .weight(1f)
                        .then(Modifier.consumeWindowInsets(NavigationBarDefaults.windowInsets)),
                )

                if (!shouldShowNavRail) {
                    BottomBar(component = component)
                }
            }
        }
    }
}

@Composable
private fun Children(
    component: HomeComponent,
    windowSizeClass: WindowSizeClass,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val wearUiState = WearUiState()

    val topBarActions: @Composable RowScope.() -> Unit = {
        AccountIcon(
            onSwitchConference = component::onSwitchConferenceClicked,
            onSignIn = component::onSignInClicked,
            onSignOut = component::onSignOutClicked,
            onShowSettings = component::onShowSettingsClicked,
            info = component.user?.let { user ->
                AccountInfo(photoUrl = user.photoUrl)
            },
            installOnWear = {}, // FIXME: handle
            wearSettingsUiState = wearUiState,
        )
    }

    Children(
        stack = component.stack,
        modifier = modifier,
        animation = stackAnimation(fade()),
    ) {
        when (val child = it.instance) {
            is HomeComponent.Child.Sessions ->
                SessionsRoute(
                    component = child.component,
                    windowSizeClass = windowSizeClass,
                    topBarActions = topBarActions,
                    snackbarHostState = snackbarHostState,
                )

            is HomeComponent.Child.MultiPane -> Text(text = "Multi-pane mode is not yet supported")

            is HomeComponent.Child.Speakers ->
                SpeakersRoute(
                    component = child.component,
                    windowSizeClass = windowSizeClass,
                    topBarActions = topBarActions,
                )

            is HomeComponent.Child.Bookmarks ->
                BookmarksRoute(
                    component = child.component,
                    windowSizeClass = windowSizeClass,
                    topBarActions = topBarActions,
                )

            is HomeComponent.Child.Search ->
                SearchRoute(
                    component = child.component,
                    windowSizeClass = windowSizeClass,
                    topBarActions = topBarActions,
                )
        }
    }
}

@Composable
private fun BottomBar(component: HomeComponent) {
    Column {
        Divider()
        NavigationBar(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            tonalElevation = 0.dp,
        ) {
            NavigationButtons(component = component) { isSelected, selectedIcon, unselectedIcon, textId, onClick ->
                NavigationBarItem(
                    selected = isSelected,
                    onClick = onClick,
                    icon = {
                        Icon(
                            imageVector = if (isSelected) selectedIcon else unselectedIcon,
                            contentDescription = stringResource(textId),
                        )
                    },
                    label = { Text(stringResource(textId)) },
                )
            }
        }
    }
}

@Composable
private fun NavigationRail(component: HomeComponent) {
    NavigationRail(
        modifier = Modifier.safeDrawingPadding(),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        NavigationButtons(component = component) { isSelected, selectedIcon, unselectedIcon, textId, onClick ->
            NavigationRailItem(
                selected = isSelected,
                onClick = onClick,
                icon = {
                    Icon(
                        imageVector = if (isSelected) selectedIcon else unselectedIcon,
                        contentDescription = stringResource(textId),
                    )
                },
            )
        }
    }
}

@Composable
private fun <T> T.NavigationButtons(
    component: HomeComponent,
    content: @Composable T.(
        isSelected: Boolean,
        selectedIcon: ImageVector,
        unselectedIcon: ImageVector,
        textId: Int,
        onClick: () -> Unit,
    ) -> Unit,
) {
    val stack by component.stack.subscribeAsState()
    val activeChild = stack.active.instance

    content(
        isSelected = activeChild is HomeComponent.Child.Sessions,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
        textId = R.string.schedule,
        onClick = component::onSessionsTabClicked,
    )

    content(
        isSelected = activeChild is HomeComponent.Child.Speakers,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        textId = R.string.speakers,
        onClick = component::onSpeakersTabClicked,
    )

    content(
        isSelected = activeChild is HomeComponent.Child.Bookmarks,
        selectedIcon = Icons.Filled.Bookmarks,
        unselectedIcon = Icons.Outlined.Bookmarks,
        textId = R.string.bookmarks,
        onClick = component::onBookmarksTabClicked,
    )

    content(
        isSelected = activeChild is HomeComponent.Child.Search,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        textId = R.string.search,
        onClick = component::onSearchTabClicked,
    )
}
