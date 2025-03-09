package dev.johnoreilly.confetti.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.decompose.AppComponent.Child
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface AppComponent {
    val stack: Value<ChildStack<*, Child>>

    fun setUser(user: User?)

    sealed class Child {
        object Loading : Child()
        class Conferences(val component: ConferencesComponent) : Child()
        class Conference(val component: ConferenceComponent) : Child()
    }
}

class DefaultAppComponent(
    componentContext: ComponentContext,
    private val onSignOut: () -> Unit,
    private val onSignIn: () -> Unit,
    initialConferenceId: String? = null,
    private val settingsComponent: SettingsComponent? = null
) : AppComponent, KoinComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope()
    private val authentication: Authentication by inject()
    val appSettings: AppSettings by inject()
    private val repository: ConfettiRepository by inject()
    private val navigation = StackNavigation<Config>()

    private var user: User? = null

    private val defaultSettingsComponent = DefaultSettingsComponent(
        componentContext = componentContext,
        appSettings = appSettings,
        authentication = authentication
    )


    override val stack: Value<ChildStack<*, Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Loading,
            childFactory = ::child,
        )

    init {
        coroutineScope.launch {
            authentication.currentUser
                .collect {
                    setUser(it)
                }
        }

        coroutineScope.launch {
            if (initialConferenceId != null) {
                selectAndNavigateToDeepLinkedConference(initialConferenceId)
            } else {
                val conference: String = repository.getConference()
                if (conference == AppSettings.CONFERENCE_NOT_SET) {
                    showConferences()
                } else {
                    val conferenceThemeColor = repository.getConferenceThemeColor()
                    showConference(conference = conference, conferenceThemeColor = conferenceThemeColor)

                    // Take the opportunity to update any listeners of the conference
                    repository.updateConfenceListeners(conference, conferenceThemeColor)
                }
            }
        }
    }


    override fun setUser(user: User?) {
        this.user = user
        onUserChanged(user?.uid)
    }

    private suspend fun selectAndNavigateToDeepLinkedConference(conferenceId: String) {
        // todo, consider changing how conference theme colors are decided so that only knowing the conference
        //  ID is enough to also get the right color
        repository.setConference(conference = conferenceId, conferenceThemeColor = null)
        showConference(conference = conferenceId, conferenceThemeColor = null)
    }

    private fun onUserChanged(uid: String?) {
        navigation.navigate { oldStack ->
            oldStack.map { config ->
                when (config) {
                    is Config.Conference -> config.copy(uid = uid)
                    else -> config
                }
            }
        }
    }

    private fun child(config: Config, componentContext: ComponentContext): Child =
        when (config) {
            is Config.Loading -> Child.Loading

            is Config.Conferences ->
                Child.Conferences(
                    DefaultConferencesComponent(
                        componentContext = componentContext,
                        onConferenceSelected = { conference ->
                            coroutineScope.launch {
                                repository.setConference(conference = conference.id, conferenceThemeColor = conference.themeColor)
                            }
                            showConference(conference = conference.id, conferenceThemeColor = conference.themeColor)
                        },
                    )
                )

            is Config.Conference ->
                Child.Conference(
                    DefaultConferenceComponent(
                        componentContext = componentContext,
                        user = user, 
                        conference = config.conference,
                        conferenceThemeColor = config.conferenceThemeColor,
                        onSwitchConference = ::showConferences,
                        onSignOut = {
                            onSignOut()
                            authentication.signOut()
                        },
                        onSignIn = onSignIn,
                        settingsComponent = defaultSettingsComponent
                    )
                )
        }

    private fun showConferences() {
        navigation.replaceAll(Config.Conferences)
    }

    private fun showConference(conference: String, conferenceThemeColor: String?) {
        navigation.replaceAll(Config.Conference(uid = user?.uid, conference = conference, conferenceThemeColor = conferenceThemeColor))
    }

    @Serializable
    private sealed class Config {
        @Serializable
        data object Loading : Config()

        @Serializable
        data object Conferences : Config()

        @Serializable
        data class Conference(
            val uid: String?, // Unused, but needed to recreated the component when the user changes
            val conference: String,
            val conferenceThemeColor: String?,
        ) : Config()
    }
}
