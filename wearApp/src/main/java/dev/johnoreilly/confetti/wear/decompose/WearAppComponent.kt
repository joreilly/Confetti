package dev.johnoreilly.confetti.wear.decompose

import android.content.Intent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.arkivanov.essenty.parcelable.TypeParceler
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.decompose.coroutineScope
import dev.johnoreilly.confetti.wear.conferences.ConferencesComponent
import dev.johnoreilly.confetti.wear.conferences.DefaultConferencesComponent
import dev.johnoreilly.confetti.wear.decompose.WearAppComponent.Child
import dev.johnoreilly.confetti.wear.sessions.ConferenceSessionsComponent
import dev.johnoreilly.confetti.wear.sessions.DefaultConferenceSessionsComponent
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface WearAppComponent {
    fun navigateUp()
    fun handleDeeplink(intent: Intent)

    val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        object Loading : Child()
        class Conferences(val component: ConferencesComponent) : Child()
        class ConferenceSessions(val component: ConferenceSessionsComponent) : Child()
    }
}

class DefaultWearAppComponent(
    componentContext: ComponentContext,
) : WearAppComponent, KoinComponent, ComponentContext by componentContext {

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
                    is Config.ConferenceSessions -> config.copy(uid = uid)
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

            is Config.ConferenceSessions ->
                Child.ConferenceSessions(
                    DefaultConferenceSessionsComponent(
                        componentContext = componentContext,
                        conference = config.conference,
                        date = config.date,
                        user = user,
                        onSessionSelected = {
                            // TODO
//                            navigation.push()
                        }
                    )
                )
        }

    private fun showConferences() {
        navigation.replaceAll(Config.Conferences)
    }

    private fun showConference(conference: String) {
        navigation.replaceAll(Config.ConferenceSessions(uid = user?.uid, conference = conference))
    }

    override fun navigateUp() {
        navigation.pop()
    }

    override fun handleDeeplink(intent: Intent) {
        println("TODO handleDeeplink $intent")
    }

    @Parcelize
    private sealed class Config : Parcelable {
        object Loading : Config()
        object Conferences : Config()

        data class ConferenceSessions(
            val uid: String?, // Unused, but needed to recreated the component when the user changes
            val conference: String,
            @TypeParceler<LocalDate?, LocalDateParceler>() val date: LocalDate? = null
        ) : Config()
    }
}
