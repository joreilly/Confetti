package dev.johnoreilly.confetti.ui.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import confetti.shared.generated.resources.Res
import confetti.shared.generated.resources.report_issue
import confetti.shared.generated.resources.report_issue_desc
import confetti.shared.generated.resources.settings_title
import confetti.shared.generated.resources.sign_in_lowercase
import confetti.shared.generated.resources.sign_out
import confetti.shared.generated.resources.switch_conference
import confetti.shared.generated.resources.venue
import dev.johnoreilly.confetti.decompose.AccountComponent
import dev.johnoreilly.confetti.decompose.AccountUiState
import dev.johnoreilly.confetti.ui.HomeScaffold
import dev.johnoreilly.confetti.ui.LocalBottomNavigationPadding
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.icons.Github
import dev.johnoreilly.confetti.ui.icons.Slack
import org.jetbrains.compose.resources.stringResource

import dev.johnoreilly.confetti.ui.component.ErrorView
import dev.johnoreilly.confetti.ui.component.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountUI(
    component: AccountComponent,
    windowSizeClass: WindowSizeClass,
    topBarNavigationIcon: @Composable () -> Unit = {},
    topBarActions: @Composable RowScope.() -> Unit = {},
) {
    val uiState by component.uiState.subscribeAsState()

    HomeScaffold(
        title = "Account",
        windowSizeClass = windowSizeClass,
        topBarNavigationIcon = topBarNavigationIcon,
        topBarActions = topBarActions,
    ) {
        when (val state = uiState) {
            is AccountUiState.Success -> {
                AccountView(
                    state = state,
                    onVenueClicked = component::onVenueClicked,
                    onSwitchConferenceClicked = component::onSwitchConferenceClicked,
                    onSettingsClicked = component::onSettingsClicked,
                    onSignInClicked = component::onSignInClicked,
                    onSignOutClicked = component::onSignOutClicked,
                )
            }
            is AccountUiState.Loading -> LoadingView()
            is AccountUiState.Error -> ErrorView {}
        }
    }
}

@Composable
fun AccountView(
    state: AccountUiState.Success,
    onVenueClicked: () -> Unit,
    onSwitchConferenceClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 16.dp + LocalBottomNavigationPadding.current
            ),
    ) {
        // 1. Permanent Conference Header
        ConferenceHeader(state = state)

        Spacer(modifier = Modifier.height(16.dp))

        // 2. User Profile Card
        UserAccountCard(
            state = state,
            onSignInClicked = onSignInClicked,
            onSignOutClicked = onSignOutClicked,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Grouped Conference Actions Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                if (state.hasVenue) {
                    AccountGroupItem(
                        title = stringResource(Res.string.venue),
                        subtitle = "Location and venue details",
                        icon = Icons.Outlined.LocationOn,
                        onClick = onVenueClicked,
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                }

                AccountGroupItem(
                    title = stringResource(Res.string.switch_conference),
                    subtitle = "Change selected conference",
                    icon = Icons.Outlined.MeetingRoom,
                    onClick = onSwitchConferenceClicked,
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )

                AccountGroupItem(
                    title = stringResource(Res.string.settings_title),
                    subtitle = "Theme and notifications",
                    icon = Icons.Outlined.Settings,
                    onClick = onSettingsClicked,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Support & Feedback Group Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AccountGroupItem(
                title = stringResource(Res.string.report_issue),
                subtitle = stringResource(Res.string.report_issue_desc),
                icon = Icons.Outlined.BugReport,
                trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                onClick = { uriHandler.openUri("https://github.com/joreilly/Confetti/issues/new") },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Community Section (Chips)
        Text(
            text = "Community",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AssistChip(
                onClick = { uriHandler.openUri("https://kotlinlang.slack.com/archives/C051P2HUVKP") },
                label = { Text("Kotlin Slack") },
                leadingIcon = {
                    Icon(
                        imageVector = ConfettiIcons.Slack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )

            AssistChip(
                onClick = { uriHandler.openUri("https://github.com/joreilly/Confetti") },
                label = { Text("GitHub") },
                leadingIcon = {
                    Icon(
                        imageVector = ConfettiIcons.Github,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
        }
    }
}

@Composable
private fun ConferenceHeader(state: AccountUiState.Success) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = state.conferenceName.take(1).ifEmpty { "C" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.conferenceName.ifEmpty { "Confetti" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = state.conferenceDays.ifEmpty { "Confetti Conference App" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UserAccountCard(
    state: AccountUiState.Success,
    onSignInClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (state.user != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.user.photoUrl != null) {
                    AsyncImage(
                        model = state.user.photoUrl,
                        contentDescription = state.user.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape),
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = state.user.email ?: "Signed in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onSignOutClicked,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.sign_out),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.sign_in_lowercase).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Sync bookmarks across devices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSignInClicked,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.sign_in_lowercase).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountGroupItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AccountViewSignedOutPreview() {
    AccountView(
        state = AccountUiState.Success(
            conferenceName = "KotlinConf 2026",
            conferenceDays = "May 20 - 22, 2026",
            user = null,
            hasVenue = true,
        ),
        onVenueClicked = {},
        onSwitchConferenceClicked = {},
        onSettingsClicked = {},
        onSignInClicked = {},
        onSignOutClicked = {},
    )
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AccountViewSignedInPreview() {
    AccountView(
        state = AccountUiState.Success(
            conferenceName = "KotlinConf 2026",
            conferenceDays = "May 20 - 22, 2026",
            user = object : dev.johnoreilly.confetti.auth.User {
                override val name = "John Doe"
                override val email = "john.doe@example.com"
                override val photoUrl = null
                override val uid = "123"
                override suspend fun token(forceRefresh: Boolean) = null
            },
            hasVenue = true,
        ),
        onVenueClicked = {},
        onSwitchConferenceClicked = {},
        onSettingsClicked = {},
        onSignInClicked = {},
        onSignOutClicked = {},
    )
}
