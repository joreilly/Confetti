package dev.johnoreilly.confetti.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.decompose.AppComponent.Child
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface AppComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        object Loading : Child()
        class Conferences(val component: ConferencesComponent) : Child()
        class Conference(val component: ConferenceComponent) : Child()
    }
}

class DefaultAppComponent(
    componentContext: ComponentContext,
    private val onSignOut: () -> Unit,
    private val isMultiPane: Boolean = false,
) : AppComponent, KoinComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope()
    private val authentication: Authentication by inject()
    private val repository: ConfettiRepository by inject()
    private val navigation = StackNavigation<Config>()

    private val user: User? get() = authentication.currentUser.value

    override val stack: Value<ChildStack<*, Child>> =
        childStack(
            source = navigation,
            initialConfiguration = Config.Loading,
            childFactory = ::child,
        )

    init {
        coroutineScope.launch {
            val conference: String = repository.getConference()
            if (conference == AppSettings.CONFERENCE_NOT_SET) {
                showConferences()
            } else {
                showConference(conference = conference)
            }
        }

        coroutineScope.launch {
            authentication.currentUser
                .map { it?.uid }
                .distinctUntilChanged()
                .collect(::onUserChanged)
        }
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
                                repository.setConference(conference = conference.id)
                            }
                            showConference(conference = conference.id)
                        },
                    )
                )

            is Config.Conference ->
                Child.Conference(
                    DefaultConferenceComponent(
                        componentContext = componentContext,
                        user = authentication.currentUser.value,
                        conference = config.conference,
                        isMultiPane = isMultiPane,
                        onSwitchConference = ::showConferences,
                        onSignOut = {
                            onSignOut()
                            authentication.signOut()
                        },
                    )
                )
        }

    private fun showConferences() {
        navigation.replaceAll(Config.Conferences)
    }

    private fun showConference(conference: String) {
        navigation.replaceAll(Config.Conference(uid = user?.uid, conference = conference))
    }

    @Parcelize
    private sealed class Config : Parcelable {
        object Loading : Config()
        object Conferences : Config()

        data class Conference(
            val uid: String?, // Unused, but needed to recreated the component when the user changes
            val conference: String,
        ) : Config()
    }
}
