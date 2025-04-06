package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.bookmarks
import confetti.shared.generated.resources.schedule
import confetti.shared.generated.resources.speakers
import confetti.shared.generated.resources.venue
import dev.johnoreilly.confetti.ui.bookmarks.BookmarksUI
import dev.johnoreilly.confetti.decompose.AppComponent
import dev.johnoreilly.confetti.decompose.ConferenceComponent
import dev.johnoreilly.confetti.decompose.DefaultAppComponent
import dev.johnoreilly.confetti.decompose.HomeComponent
import dev.johnoreilly.confetti.ui.search.SearchUI
import dev.johnoreilly.confetti.ui.settings.SettingsUI
import dev.johnoreilly.confetti.ui.component.LoadingView
import dev.johnoreilly.confetti.ui.sessions.SessionDetailsUI
import dev.johnoreilly.confetti.ui.sessions.SessionsUI
import dev.johnoreilly.confetti.ui.speakers.SpeakerDetailsUI
import dev.johnoreilly.confetti.ui.speakers.SpeakersUI
import dev.johnoreilly.confetti.ui.venue.VenueUI
import dev.johnoreilly.confetti.utils.isExpanded
import org.jetbrains.compose.resources.stringResource

@Composable
fun App(component: DefaultAppComponent) {
    Children(stack = component.stack) {
        when (val child = it.instance) {
            is AppComponent.Child.Loading -> LoadingView()
            is AppComponent.Child.Conferences -> ConferenceListView(child.component)
            is AppComponent.Child.Conference -> ConferenceView(child.component)
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun ConferenceView(component: ConferenceComponent) {
    ConferenceMaterialTheme(component.conferenceThemeColor) {
        ChildStack(
            stack = component.stack,
            animation = predictiveBackAnimation(
                backHandler = component.backHandler,
                onBack = component::onBackClicked,
            ),
        ) {
            when (val child = it.instance) {
                is ConferenceComponent.Child.Home -> HomeView(child.component)
                is ConferenceComponent.Child.SessionDetails -> SessionDetailsUI(child.component)
                is ConferenceComponent.Child.SpeakerDetails -> SpeakerDetailsUI(child.component)
                is ConferenceComponent.Child.Settings -> {
                    child.component?.let { childComponent ->
                        SettingsUI(childComponent, component::onBackClicked)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeView(component: HomeComponent) {
    val windowSizeClass = calculateWindowSizeClass()
    val shouldShowNavRail = windowSizeClass.isExpanded
    val snackbarHostState = remember { SnackbarHostState() }

    Row {
        if (shouldShowNavRail) {
            NavigationRail(component)
        }

        val topBarNavigationIcon = @Composable {
            AccountIcon(
                onSwitchConference = component::onSwitchConferenceClicked,
                onGetRecommendations = component::onGetRecommendationsClicked,
                onSignIn = component::onSignInClicked,
                onSignOut = component::onSignOutClicked,
                onShowSettings = component::onShowSettingsClicked,
                info = component.user?.let { user ->
                    AccountInfo(photoUrl = user.photoUrl)
                },
                installOnWear = {}, // FIXME: handle
                //wearSettingsUiState = wearUiState,
                showRecommendationsOption = component.isGeminiEnabled()
            )
        }

        val topBarActions: @Composable RowScope.() -> Unit = {
            IconButton(onClick = { component.onSearchClicked() }) {
                Icon(Icons.Outlined.Search, contentDescription = "search")
            }
        }

        Scaffold(
            bottomBar = { if (!shouldShowNavRail) BottomBar(component) },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {

                Children(stack = component.stack) {
                    when (val child = it.instance) {
                        is HomeComponent.Child.Sessions ->
                            SessionsUI(
                                component = child.component,
                                windowSizeClass = windowSizeClass,
                                topBarNavigationIcon = topBarNavigationIcon,
                                topBarActions = topBarActions,
                                snackbarHostState = snackbarHostState
                            )

                        is HomeComponent.Child.Speakers -> SpeakersUI(child.component)
                        is HomeComponent.Child.Bookmarks ->
                            BookmarksUI(
                                component = child.component,
                                windowSizeClass = windowSizeClass,
                                topBarNavigationIcon = topBarNavigationIcon,
                                topBarActions = topBarActions,
                            )

                        is HomeComponent.Child.Venue -> VenueUI(child.component)

                        is HomeComponent.Child.Search ->
                            SearchUI(
                                component = child.component,
                                windowSizeClass = windowSizeClass,
                                topBarNavigationIcon = topBarNavigationIcon,
                                topBarActions = topBarActions,
                            )

                        is HomeComponent.Child.Recommendations -> GeminiQueryView(child.component)
                    }
                }

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
        NavigationButtons(component = component) { isSelected, selectedIcon, unselectedIcon, text, onClick ->
            NavigationRailItem(
                selected = isSelected,
                onClick = onClick,
                icon = {
                    Icon(
                        imageVector = if (isSelected) selectedIcon else unselectedIcon,
                        contentDescription = text
                    )
                },
            )
        }
    }
}


@Composable
private fun BottomBar(component: HomeComponent) {
    Column {
        HorizontalDivider()
        NavigationBar(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            tonalElevation = 0.dp,
        ) {
            NavigationButtons(component = component) { isSelected, selectedIcon, unselectedIcon, text, onClick ->
                NavigationBarItem(
                    selected = isSelected,
                    onClick = onClick,
                    icon = {
                        Icon(
                            imageVector = if (isSelected) selectedIcon else unselectedIcon,
                            contentDescription = text,
                        )
                    },
                    label = { Text(text) },
                )
            }
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
        text: String,
        onClick: () -> Unit,
    ) -> Unit,
) {
    val stack by component.stack.subscribeAsState()
    val activeChild = stack.active.instance

    content(
        activeChild is HomeComponent.Child.Sessions,
        Icons.Filled.Home,
        Icons.Outlined.Home,
        stringResource(Res.string.schedule),
        component::onSessionsTabClicked,
    )

    content(
        activeChild is HomeComponent.Child.Speakers,
        Icons.Filled.Person,
        Icons.Outlined.Person,
        stringResource(Res.string.speakers),
        component::onSpeakersTabClicked,
    )

    content(
        activeChild is HomeComponent.Child.Bookmarks,
        Icons.Filled.Bookmarks,
        Icons.Outlined.Bookmarks,
        stringResource(Res.string.bookmarks),
        component::onBookmarksTabClicked,
    )

    content(
        activeChild is HomeComponent.Child.Venue,
        Icons.Filled.LocationOn,
        Icons.Outlined.LocationOn,
        stringResource(Res.string.venue),
        component::onVenueTabClicked,
    )

//    content(
//        activeChild is HomeComponent.Child.Recommendations,
//        Icons.Filled.Search,
//        Icons.Outlined.Search,
//        stringResource(Res.string.gemini),
//        component::onGetRecommendationsClicked,
//    )
}
