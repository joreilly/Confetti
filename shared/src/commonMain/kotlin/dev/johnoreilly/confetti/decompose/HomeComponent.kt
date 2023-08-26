package dev.johnoreilly.confetti.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.johnoreilly.confetti.decompose.HomeComponent.Child
import dev.johnoreilly.confetti.auth.User

interface HomeComponent {

    val user: User?
    val stack: Value<ChildStack<*, Child>>

    fun onSessionsTabClicked()
    fun onSpeakersTabClicked()
    fun onBookmarksTabClicked()
    fun onSearchTabClicked()
    fun onSwitchConferenceClicked()
    fun onSignInClicked()
    fun onSignOutClicked()
    fun onShowSettingsClicked()

    sealed class Child {
        class Sessions(val component: SessionsComponent) : Child()
        class MultiPane(val component: MultiPaneComponent) : Child()
        class Speakers(val component: SpeakersComponent) : Child()
        class Bookmarks(val component: BookmarksComponent) : Child()
        class Search(val component: SearchComponent) : Child()
    }
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val conference: String,
    override val user: User?,
    private val isMultiPane: Boolean,
    private val onSwitchConference: () -> Unit,
    private val onSessionSelected: (id: String) -> Unit,
    private val onSpeakerSelected: (id: String) -> Unit,
    private val onSignIn: () -> Unit,
    private val onSignOut: () -> Unit,
    private val onShowSettings: () -> Unit,
) : HomeComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, Child>> =
        childStack(
            source = navigation,
            initialConfiguration = Config.Sessions,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, componentContext: ComponentContext): Child =
        when (config) {
            Config.Sessions ->
                if (isMultiPane) {
                    Child.MultiPane(
                        DefaultMultiPaneComponent(
                            componentContext = componentContext,
                            conference = conference,
                            user = user,
                            onSignIn = onSignIn,
                            onSpeakerSelected = onSpeakerSelected,
                        )
                    )
                } else {
                    Child.Sessions(
                        DefaultSessionsComponent(
                            componentContext = componentContext,
                            conference = conference,
                            user = user,
                            onSessionSelected = onSessionSelected,
                            onSignIn = onSignIn,
                        )
                    )
                }

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

    override fun onSessionsTabClicked() {
        navigation.bringToFront(Config.Sessions)
    }

    override fun onSpeakersTabClicked() {
        navigation.bringToFront(Config.Speakers)
    }

    override fun onBookmarksTabClicked() {
        navigation.bringToFront(Config.Bookmarks)
    }

    override fun onSearchTabClicked() {
        navigation.bringToFront(Config.Search)
    }

    override fun onSwitchConferenceClicked() {
        onSwitchConference()
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

    @Parcelize
    private sealed class Config : Parcelable {
        object Sessions : Config()
        object Speakers : Config()
        object Bookmarks : Config()
        object Search : Config()
    }
}
