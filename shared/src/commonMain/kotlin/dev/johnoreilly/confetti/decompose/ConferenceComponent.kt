package dev.johnoreilly.confetti.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.johnoreilly.confetti.decompose.ConferenceComponent.Child
import dev.johnoreilly.confetti.auth.User
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ConferenceComponent : BackHandlerOwner {

    val stack: Value<ChildStack<*, Child>>

    fun onBackClicked()
    fun onBackClicked(toIndex: Int)

    sealed class Child {
        class Home(val component: HomeComponent) : Child()
        class SessionDetails(val component: SessionDetailsComponent) : Child()
        class SpeakerDetails(val component: SpeakerDetailsComponent) : Child()
        class SignIn(val component: SignInComponent) : Child()
        class Settings(val component: SettingsComponent) : Child()
    }
}

class DefaultConferenceComponent(
    componentContext: ComponentContext,
    private val user: User?,
    private val conference: String,
    private val isMultiPane: Boolean,
    private val onSwitchConference: () -> Unit,
    private val onSignOut: () -> Unit,
) : ConferenceComponent, KoinComponent, ComponentContext by componentContext {

    private val settingsComponent: SettingsComponent by inject()
    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, Child>> =
        childStack(
            source = navigation,
            initialConfiguration = Config.Home,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, componentContext: ComponentContext): Child =
        when (config) {
            is Config.Home ->
                Child.Home(
                    DefaultHomeComponent(
                        componentContext = componentContext,
                        conference = conference,
                        user = user,
                        isMultiPane = isMultiPane,
                        onSwitchConference = onSwitchConference,
                        onSessionSelected = { navigation.push(Config.SessionDetails(sessionId = it)) },
                        onSpeakerSelected = { navigation.push(Config.SpeakerDetails(speakerId = it)) },
                        onSignIn = { navigation.push(Config.SignIn) },
                        onSignOut = onSignOut,
                        onShowSettings = { navigation.push(Config.Settings) },
                    )
                )

            is Config.SessionDetails ->
                Child.SessionDetails(
                    DefaultSessionDetailsComponent(
                        componentContext = componentContext,
                        conference = conference,
                        sessionId = config.sessionId,
                        user = user,
                        onFinished = navigation::pop,
                        onSignIn = { navigation.push(Config.SignIn) },
                        onSpeakerSelected = { navigation.bringToFront(Config.SpeakerDetails(speakerId = it)) },
                    )
                )

            is Config.SpeakerDetails ->
                Child.SpeakerDetails(
                    DefaultSpeakerDetailsComponent(
                        componentContext = componentContext,
                        conference = conference,
                        speakerId = config.speakerId,
                        onSessionSelected = { navigation.bringToFront(Config.SessionDetails(sessionId = it)) },
                        onFinished = navigation::pop,
                    )
                )

            is Config.SignIn ->
                Child.SignIn(
                    DefaultSignInComponent(
                        componentContext = componentContext,
                        onClosed = navigation::pop,
                    )
                )

            is Config.Settings -> Child.Settings(settingsComponent)
        }

    override fun onBackClicked() {
        navigation.pop()
    }

    override fun onBackClicked(toIndex: Int) {
        navigation.navigate { it.take(toIndex + 1) }
    }

    @Parcelize
    private sealed class Config : Parcelable {
        object Home : Config()
        data class SessionDetails(val sessionId: String) : Config()
        data class SpeakerDetails(val speakerId: String) : Config()
        object SignIn : Config()
        object Settings : Config()
    }
}
