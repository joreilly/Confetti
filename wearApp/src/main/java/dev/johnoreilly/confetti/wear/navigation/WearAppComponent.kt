package dev.johnoreilly.confetti.wear.navigation

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
import dev.johnoreilly.confetti.decompose.ConferencesComponent
import dev.johnoreilly.confetti.decompose.DefaultConferencesComponent
import dev.johnoreilly.confetti.decompose.DefaultSessionDetailsComponent
import dev.johnoreilly.confetti.decompose.DefaultSpeakerDetailsComponent
import dev.johnoreilly.confetti.decompose.SessionDetailsComponent
import dev.johnoreilly.confetti.decompose.SpeakerDetailsComponent
import dev.johnoreilly.confetti.decompose.coroutineScope
import dev.johnoreilly.confetti.wear.auth.DefaultFirebaseSignOutComponent
import dev.johnoreilly.confetti.wear.auth.FirebaseSignOutComponent
import dev.johnoreilly.confetti.wear.bookmarks.BookmarksComponent
import dev.johnoreilly.confetti.wear.bookmarks.DefaultBookmarksComponent
import dev.johnoreilly.confetti.wear.home.DefaultHomeComponent
import dev.johnoreilly.confetti.wear.home.HomeComponent
import dev.johnoreilly.confetti.wear.navigation.WearAppComponent.Child
import dev.johnoreilly.confetti.wear.navigation.WearAppComponent.Config
import dev.johnoreilly.confetti.wear.sessions.ConferenceSessionsComponent
import dev.johnoreilly.confetti.wear.sessions.DefaultConferenceSessionsComponent
import dev.johnoreilly.confetti.wear.settings.DefaultSettingsComponent
import dev.johnoreilly.confetti.wear.settings.SettingsComponent
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface WearAppComponent {
    fun navigateUp()
    fun handleDeeplink(intent: Intent)

    val stack: Value<ChildStack<Config, Child>>

    sealed class Child {
        object Loading : Child()
        class Conferences(val component: ConferencesComponent) : Child()
        class ConferenceSessions(val component: ConferenceSessionsComponent) : Child()

        class SessionDetails(val component: SessionDetailsComponent) : Child()

        class SpeakerDetails(val component: SpeakerDetailsComponent) : Child()

        class Settings(val component: SettingsComponent) : Child()

        object GoogleSignIn : Child()

        class GoogleSignOut(val component: FirebaseSignOutComponent) : Child()

        class Bookmarks(val component: BookmarksComponent) : Child()

        class Home(val component: HomeComponent) : Child()
    }

    @Parcelize
    sealed class Config : Parcelable {
        val loggingName: String
            get() = this::class.java.simpleName

        open val loggingArguments: Map<String, String>
            get() = mapOf()

        object Loading : Config()
        object Conferences : Config()

        data class ConferenceSessions(
            val uid: String?, // Unused, but needed to recreated the component when the user changes
            val conference: String,
            @TypeParceler<LocalDate?, LocalDateParceler>() val date: LocalDate? = null
        ) : Config() {
            override val loggingArguments: Map<String, String>
                get() = mapOf("conference" to conference)
        }

        data class SessionDetails(
            val uid: String?, // Unused, but needed to recreated the component when the user changes
            val conference: String,
            val session: String,
        ) : Config() {
            override val loggingArguments: Map<String, String>
                get() = mapOf("conference" to conference, "session" to session)
        }

        data class SpeakerDetails(
            val uid: String?, // Unused, but needed to recreated the component when the user changes
            val conference: String,
            val speaker: String,
        ) : Config() {
            override val loggingArguments: Map<String, String>
                get() = mapOf("conference" to conference, "speaker" to speaker)
        }

        object Settings : Config()

        object GoogleSignIn : Config()

        object GoogleSignOut : Config()

        data class Bookmarks(
            val uid: String?, // Unused, but needed to recreated the component when the user changes
            val conference: String,
        ) : Config() {
            override val loggingArguments: Map<String, String>
                get() = mapOf("conference" to conference)
        }

        data class Home(
            val uid: String?, // Unused, but needed to recreated the component when the user changes
            val conference: String,
        ) : Config() {
            override val loggingArguments: Map<String, String>
                get() = mapOf("conference" to conference)
        }
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

    override val stack: Value<ChildStack<Config, Child>> =
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
                            navigation.push(Config.SessionDetails(user?.uid, config.conference, it))
                        }
                    )
                )

            is Config.SessionDetails ->
                Child.SessionDetails(
                    DefaultSessionDetailsComponent(
                        componentContext = componentContext,
                        conference = config.conference,
                        sessionId = config.session,
                        onSpeakerSelected = {
                            navigation.push(Config.SessionDetails(user?.uid, config.conference, it))
                        },
                        user = user
                    )
                )

            is Config.SpeakerDetails ->
                Child.SpeakerDetails(
                    DefaultSpeakerDetailsComponent(
                        componentContext = componentContext,
                        conference = config.conference,
                        speakerId = config.speaker,
                        onSessionSelected = {
                            navigation.push(Config.SpeakerDetails(user?.uid, config.conference, it))
                        },
                    )
                )

            is Config.Settings -> Child.Settings(
                DefaultSettingsComponent(
                    componentContext,
                    onNavigateToGoogleSignIn = {
                        navigation.push(Config.GoogleSignIn)
                    },
                    onNavigateToGoogleSignOut = {
                        navigation.push(Config.GoogleSignOut)
                    })
            )

            is Config.GoogleSignIn -> Child.GoogleSignIn

            is Config.GoogleSignOut -> Child.GoogleSignOut(
                DefaultFirebaseSignOutComponent(
                    componentContext,
                    onSignedOut = { onUserChanged(null) },
                    navigateUp = { navigateUp() }
                )
            )

            is Config.Home -> Child.Home(
                DefaultHomeComponent(
                    componentContext,
                    config.conference,
                    user,
                    onSessionSelected = {
                        navigation.push(Config.SessionDetails(user?.uid, config.conference, it))
                    },
                    onDaySelected = {
                        navigation.push(Config.ConferenceSessions(config.uid, config.conference, it))
                    },
                    onSettingsSelected = {
                        navigation.push(Config.Settings)
                    },
                    onBookmarksToggled = {
                        TODO("onBookmarksToggled")
                    }
                )
            )

            is Config.Bookmarks -> Child.Bookmarks(
                DefaultBookmarksComponent(
                    componentContext,
                    config.conference,
                    user,
                    onSessionSelected = {
                        navigation.push(Config.SessionDetails(user?.uid, config.conference, it))
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
}
