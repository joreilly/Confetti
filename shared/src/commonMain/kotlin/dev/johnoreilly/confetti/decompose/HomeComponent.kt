package dev.johnoreilly.confetti.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.childContext
import dev.johnoreilly.confetti.BuildKonfig
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.decompose.HomeComponent.Child
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import dev.johnoreilly.confetti.AppSettings

interface HomeComponent {

    val conference: String
    val user: User?
    val stack: Value<ChildStack<*, Child>>

    suspend fun isAgentEnabled(): Boolean
    fun onSessionsTabClicked()
    fun onSpeakersTabClicked()
    fun onBookmarksTabClicked()
    fun onVenueTabClicked()
    fun onSearchClicked()
    fun onSwitchConferenceClicked()
    fun onAgentClicked()
    fun onSignInClicked()
    fun onSignOutClicked()
    fun onShowSettingsClicked()
    fun onBackClicked()

    sealed class Child {
        class Sessions(val component: SessionsComponent) : Child()
        class Speakers(val component: SpeakersComponent) : Child()
        class Bookmarks(val component: BookmarksComponent) : Child()
        class Venue(val component: VenueComponent) : Child()
        class Search(val component: SearchComponent) : Child()
    }
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    override val conference: String,
    override val user: User?,
    private val onSwitchConference: () -> Unit,
    private val onSessionSelected: (id: String) -> Unit,
    private val onSpeakerSelected: (id: String) -> Unit,
    private val onSignIn: () -> Unit,
    private val onSignOut: () -> Unit,
    private val onShowSettings: () -> Unit,
    private val onShowAgent: () -> Unit,
) : HomeComponent, KoinComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Sessions,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, componentContext: ComponentContext): Child =
        when (config) {
            Config.Sessions ->
                Child.Sessions(
                    DefaultSessionsComponent(
                        componentContext = componentContext,
                        conference = conference,
                        user = user,
                        onSessionSelected = onSessionSelected,
                        onSignIn = onSignIn,
                    )
                )

            Config.Speakers ->
                Child.Speakers(
                    DefaultSpeakersComponent(
                        componentContext = componentContext,
                        conference = conference,
                        onSpeakerSelected = onSpeakerSelected,
                    )
                )

            Config.Bookmarks ->
                Child.Bookmarks(
                    DefaultBookmarksComponent(
                        componentContext = componentContext,
                        conference = conference,
                        user = user,
                        onSessionSelected = onSessionSelected,
                        onSignIn = onSignIn,
                    )
                )

            Config.Venue ->
                Child.Venue(
                    DefaultVenueComponent(
                        componentContext = componentContext,
                        conference = conference
                    )
                )

            Config.Search ->
                Child.Search(
                    DefaultSearchComponent(
                        componentContext = componentContext,
                        conference = conference,
                        user = user,
                        onSessionSelected = onSessionSelected,
                        onSpeakerSelected = onSpeakerSelected,
                        onSignIn = onSignIn,
                    )
                )
        }

    override suspend fun isAgentEnabled(): Boolean {
        val appSettings: AppSettings by inject()
        val forceEnable = appSettings.settings.getBoolean(AppSettings.FORCE_ENABLE_ASSISTANT, false)
        return forceEnable || BuildKonfig.GEMINI_API_KEY.isNotEmpty()
    }

    override fun onSessionsTabClicked() {
        navigation.bringToFront(Config.Sessions)
    }

    override fun onSpeakersTabClicked() {
        navigation.bringToFront(Config.Speakers)
    }

    override fun onBookmarksTabClicked() {
        navigation.bringToFront(Config.Bookmarks)
    }

    override fun onVenueTabClicked() {
        navigation.bringToFront(Config.Venue)
    }

    override fun onSearchClicked() {
        navigation.bringToFront(Config.Search)
    }

    override fun onSwitchConferenceClicked() {
        onSwitchConference()
    }

    override fun onAgentClicked() {
        onShowAgent()
    }

    override fun onSignInClicked() {
        onSignIn()
    }

    override fun onSignOutClicked() {
        onSignOut()
    }

    override fun onShowSettingsClicked() {
        onShowSettings()
    }

    override fun onBackClicked() {
        navigation.pop()
    }

    @Serializable
    private sealed class Config {
        @Serializable
        data object Sessions : Config()

        @Serializable
        data object Speakers : Config()

        @Serializable
        data object Bookmarks : Config()

        @Serializable
        data object Venue : Config()

        @Serializable
        data object Search : Config()
    }
}
