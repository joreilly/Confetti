package dev.johnoreilly.confetti.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.agent_assistant
import confetti.shared.generated.resources.bookmarks
import confetti.shared.generated.resources.schedule
import confetti.shared.generated.resources.speakers
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.johnoreilly.confetti.decompose.AppComponent
import dev.johnoreilly.confetti.decompose.ConferenceComponent
import dev.johnoreilly.confetti.decompose.DefaultAppComponent
import dev.johnoreilly.confetti.decompose.HomeComponent
import dev.johnoreilly.confetti.ui.bookmarks.BookmarksUI
import dev.johnoreilly.confetti.ui.component.LoadingView
import dev.johnoreilly.confetti.ui.search.SearchUI
import dev.johnoreilly.confetti.ui.sessions.SessionDetailsUI
import dev.johnoreilly.confetti.ui.sessions.SessionsUI
import dev.johnoreilly.confetti.ui.settings.SettingsUI
import dev.johnoreilly.confetti.ui.speakers.SpeakerDetailsUI
import dev.johnoreilly.confetti.ui.speakers.SpeakersUI
import dev.johnoreilly.confetti.ui.venue.VenueUI
import dev.johnoreilly.confetti.utils.isExpanded
import org.jetbrains.compose.resources.stringResource

@Composable
fun App(component: DefaultAppComponent) {
    Children(
        stack = component.stack,
        animation = stackAnimation(animator = fade() + scale())
    ) {
        when (val child = it.instance) {
            is AppComponent.Child.Loading -> LoadingView()
            is AppComponent.Child.Onboarding -> OnboardingUI(child.component)
            is AppComponent.Child.Conferences -> ConferenceListView(child.component)
            is AppComponent.Child.Conference -> ConferenceView(child.component)
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ConferenceView(component: ConferenceComponent) {
    val windowSizeClass = calculateWindowSizeClass()
    ConferenceMaterialThemeFromSettings(component.conferenceThemeColor) {
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
                is ConferenceComponent.Child.Venue ->
                    VenueUI(
                        component = child.component,
                        windowSizeClass = windowSizeClass,
                        topBarNavigationIcon = {
                            IconButton(onClick = component::onBackClicked) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )

                is ConferenceComponent.Child.Settings -> {
                    child.component?.let { childComponent ->
                        SettingsUI(childComponent, component::onBackClicked)
                    }
                }

                is ConferenceComponent.Child.Agent -> {
                    ConferenceAgentView(
                        component = child.component,
                        onCloseClick = component::onBackClicked
                    )
                }

                is ConferenceComponent.Child.Search -> {
                    SearchUI(
                        component = child.component,
                        windowSizeClass = calculateWindowSizeClass(),
                        onBackClick = component::onBackClicked,
                    )
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
    val hazeState = remember { HazeState() }

    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Row {
            if (shouldShowNavRail) {
                NavigationRail(component)
            }

            val agentEnabled by produceState(initialValue = false) {
                value = component.isAgentEnabled()
            }

            val topBarNavigationIcon: @Composable () -> Unit = {
                if (agentEnabled) {
                    IconButton(onClick = component::onAgentClicked) {
                        Icon(
                            imageVector = Icons.Filled.Assistant,
                            contentDescription = stringResource(Res.string.agent_assistant),
                        )
                    }
                }
            }

            val topBarActions: @Composable RowScope.() -> Unit = {
                IconButton(onClick = { component.onSearchClicked() }) {
                    Icon(Icons.Outlined.Search, contentDescription = "search")
                }
            }

            Scaffold(
                bottomBar = {
                    if (!shouldShowNavRail) {
                        HazeBottomBar(hazeState = hazeState) {
                            BottomBarItems(component)
                        }
                    }
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                contentWindowInsets = WindowInsets(0.dp)
            ) { innerPadding ->
                val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val bottomPadding = if (!shouldShowNavRail) {
                    innerPadding.calculateBottomPadding()
                } else {
                    navigationBarsPadding
                }
                CompositionLocalProvider(
                    LocalBottomNavigationPadding provides bottomPadding
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {

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

                                is HomeComponent.Child.Speakers ->
                                    SpeakersUI(
                                        component = child.component,
                                        windowSizeClass = windowSizeClass,
                                        topBarNavigationIcon = topBarNavigationIcon,
                                        topBarActions = topBarActions,
                                    )

                                is HomeComponent.Child.Bookmarks ->
                                    BookmarksUI(
                                        component = child.component,
                                        windowSizeClass = windowSizeClass,
                                        topBarNavigationIcon = topBarNavigationIcon,
                                        topBarActions = topBarActions,
                                    )

                                is HomeComponent.Child.Account ->
                                    dev.johnoreilly.confetti.ui.account.AccountUI(
                                        component = child.component,
                                        windowSizeClass = windowSizeClass,
                                        topBarNavigationIcon = topBarNavigationIcon,
                                        topBarActions = topBarActions,
                                    )
                            }
                        }

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
        NavigationButtons(component = component) { isSelected, icon, text, badgeCount, onClick ->
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.12f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "NavRailItemScale"
            )
            NavigationRailItem(
                selected = isSelected,
                onClick = onClick,
                icon = {
                    Box(modifier = Modifier.scale(scale)) {
                        BadgedBox(
                            badge = {
                                if (badgeCount > 0) {
                                    Badge {
                                        val badgeText = if (badgeCount > 99) "99+" else badgeCount.toString()
                                        Text(badgeText)
                                    }
                                }
                            }
                        ) {
                            icon()
                        }
                    }
                },
                label = { Text(text) },
            )
        }
    }
}


@Composable
fun HazeBottomBar(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier) {
        HorizontalDivider()
        NavigationBar(
            modifier = Modifier.hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    tint = HazeTint(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    blurRadius = 25.dp,
                )
            ),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            tonalElevation = 0.dp,
            containerColor = Color.Transparent,
            content = content,
        )
    }
}

@Composable
private fun RowScope.BottomBarItems(component: HomeComponent) {
    NavigationButtons(component = component) { isSelected, icon, text, badgeCount, onClick ->
        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.12f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "NavItemScale"
        )
        NavigationBarItem(
            selected = isSelected,
            onClick = onClick,
            icon = {
                Box(modifier = Modifier.scale(scale)) {
                    BadgedBox(
                        badge = {
                            if (badgeCount > 0) {
                                Badge {
                                    val badgeText = if (badgeCount > 99) "99+" else badgeCount.toString()
                                    Text(badgeText)
                                }
                            }
                        }
                    ) {
                        icon()
                    }
                }
            },
            label = { Text(text) },
        )
    }
}


@Composable
private fun <T> T.NavigationButtons(
    component: HomeComponent,
    content: @Composable T.(
        isSelected: Boolean,
        icon: @Composable () -> Unit,
        text: String,
        badgeCount: Int,
        onClick: () -> Unit,
    ) -> Unit,
) {
    val stack by component.stack.subscribeAsState()
    val activeChild = stack.active.instance
    val upcomingBookmarksCount by component.upcomingBookmarksCount.collectAsStateWithLifecycle(initialValue = 0)

    content(
        activeChild is HomeComponent.Child.Sessions,
        {
            Icon(
                imageVector = if (activeChild is HomeComponent.Child.Sessions) Icons.Filled.CalendarToday else Icons.Outlined.CalendarToday,
                contentDescription = stringResource(Res.string.schedule),
            )
        },
        stringResource(Res.string.schedule),
        0,
        component::onSessionsTabClicked,
    )

    content(
        activeChild is HomeComponent.Child.Speakers,
        {
            Icon(
                imageVector = if (activeChild is HomeComponent.Child.Speakers) Icons.Filled.People else Icons.Outlined.People,
                contentDescription = stringResource(Res.string.speakers),
            )
        },
        stringResource(Res.string.speakers),
        0,
        component::onSpeakersTabClicked,
    )

    content(
        activeChild is HomeComponent.Child.Bookmarks,
        {
            Icon(
                imageVector = if (activeChild is HomeComponent.Child.Bookmarks) Icons.Filled.Bookmarks else Icons.Outlined.Bookmarks,
                contentDescription = stringResource(Res.string.bookmarks),
            )
        },
        stringResource(Res.string.bookmarks),
        upcomingBookmarksCount,
        component::onBookmarksTabClicked,
    )

    val isAccountSelected = activeChild is HomeComponent.Child.Account
    val userPhotoUrl = component.user?.photoUrl
    content(
        isAccountSelected,
        {
            if (userPhotoUrl != null) {
                AsyncImage(
                    model = userPhotoUrl,
                    contentDescription = "Account",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = if (isAccountSelected) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                    contentDescription = "Account",
                )
            }
        },
        "Account",
        0,
        component::onAccountTabClicked,
    )
}
