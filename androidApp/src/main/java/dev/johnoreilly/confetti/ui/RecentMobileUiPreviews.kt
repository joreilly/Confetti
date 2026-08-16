package dev.johnoreilly.confetti.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.decompose.AccountUiState
import dev.johnoreilly.confetti.decompose.ConferenceAgentComponent
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.preview.previewConferenceListState
import dev.johnoreilly.confetti.ui.account.AccountView

/**
 * Android-discoverable previews for the mobile UI work landed in August 2026. Shared-source
 * previews are useful in IDEs, but the Android compose-preview catalog only scans this module's
 * main classes, so these entries keep the recent Haze and navigation redesigns in visual CI.
 */
@Preview(
    name = "Light",
    widthDp = 411,
    heightDp = 914,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    widthDp = 411,
    heightDp = 914,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class RecentMobileScreen

@RecentMobileScreen
@Composable
fun HazeBottomNavigationPreview() {
    ConfettiTheme(disableDynamicTheming = true) {
        HazeBottomBarPreviewContent()
    }
}

@Preview(
    name = "Light - high contrast blur target",
    widthDp = 411,
    heightDp = 260,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark - high contrast blur target",
    widthDp = 411,
    heightDp = 260,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun HazeBottomNavigationDetailPreview() {
    ConfettiTheme(disableDynamicTheming = true) {
        HazeBottomBarContrastPreviewContent()
    }
}

@RecentMobileScreen
@Composable
fun OnboardingHazePreview() {
    ConfettiTheme(disableDynamicTheming = true) {
        OnboardingContent(
            notificationsEnabled = false,
            supportsNotifications = true,
            onNotificationsToggle = {},
            onComplete = {},
        )
    }
}

@RecentMobileScreen
@Composable
fun AccountScreenPreview() {
    ConfettiTheme(disableDynamicTheming = true) {
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
}

@RecentMobileScreen
@Composable
fun AssistantConversationPreview() {
    ConfettiTheme(disableDynamicTheming = true) {
        ConferenceAgentView(
            component = PreviewConferenceAgentComponent(),
            onCloseClick = {},
        )
    }
}

@RecentMobileScreen
@Composable
fun ConferenceSelectionPreview() {
    ConferenceListView(
        component = PreviewConferencesComponent(
            ConferencesComponent.Success(
                conferenceListByYear = previewConferenceListState.conferenceListByYear,
                currentConference = "kotlinconf2023",
            ),
        ),
    )
}

private class PreviewConferencesComponent(
    state: ConferencesComponent.UiState,
) : ConferencesComponent {
    override val uiState: Value<ConferencesComponent.UiState> = MutableValue(state)
    override val onBack: (() -> Unit)? = {}

    override fun refresh() = Unit

    override fun onConferenceClicked(conference: GetConferencesQuery.Conference) = Unit
}

private class PreviewConferenceAgentComponent : ConferenceAgentComponent {
    override val uiState: Value<ConferenceAgentComponent.UiState> = MutableValue(
        ConferenceAgentComponent.UiState(
            messages = listOf(
                ConferenceAgentComponent.Message.System(
                    "Ask about the schedule, speakers, or venue.",
                ),
                ConferenceAgentComponent.Message.User(
                    "What should I see after the opening keynote?",
                ),
                ConferenceAgentComponent.Message.ToolCall("Search sessions after 10:00"),
                ConferenceAgentComponent.Message.ToolCall("Rank bookmarked topics"),
                ConferenceAgentComponent.Message.Agent(
                    "Try the Kotlin Multiplatform session in the main hall at 11:00.",
                ),
            ),
            inputText = "Show me another option",
        ),
    )

    override fun updateInputText(text: String) = Unit

    override fun sendMessage() = Unit

    override fun restartChat() = Unit
}
