package dev.johnoreilly.confetti.wear.navigation

import android.content.Intent
import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.decompose.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface WearAppComponent {
    val config: Config

    val stack: Value<ChildStack<Config, Child>>

    fun navigateUp()

    fun handleDeeplink(intent: Intent): Boolean

    fun onUserChanged(uid: String?)

    fun showConferences()

    fun showConference(conference: String)

    fun navigateTo(config: Config)
}

class DefaultWearAppComponent(
    componentContext: ComponentContext,
    intent: Intent,
) : WearAppComponent, KoinComponent, ComponentContext by componentContext {
    internal val coroutineScope = coroutineScope()
    private val authentication: Authentication by inject()
    internal val repository: ConfettiRepository by inject()
    internal val navigation = StackNavigation<Config>()

    internal val user: User? get() = authentication.currentUser.value

    override val config: Config
        get() = stack.value.active.configuration

    override val stack: Value<ChildStack<Config, Child>> =
        childStack(
            source = navigation,
            initialStack = { initialConfig(intent) },
            childFactory = this::buildChild,
        )

    private fun initialConfig(intent: Intent) =
        (deeplinkStack(intent) ?: listOf(Config.Loading))

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

    override fun onUserChanged(uid: String?) {
        navigation.navigate { oldStack ->
            oldStack.map { config ->
                when (config) {
                    is Config.UserAware -> config.onUserChanged(uid = uid)
                    else -> config
                }
            }
        }
    }

    override fun showConferences() {
        navigation.replaceAll(Config.Conferences)
    }

    override fun showConference(conference: String) {
        navigation.replaceAll(Config.Home(uid = user?.uid, conference = conference))
    }

    override fun navigateUp() {
        navigation.pop()
    }

    override fun navigateTo(config: Config) {
        navigation.push(config)
    }

    private fun buildConfig(user: String?, uri: String): Config? {
        val path = uri.substringAfter("confetti://confetti")

        return when {
            path == "/signIn" -> Config.GoogleSignIn
            path == "/signOut" -> Config.GoogleSignOut
            path == "/settings" -> Config.Settings
            path == "/conferences" -> Config.Conferences
            path.startsWith("/home/") -> Config.Home(user, path.substringAfter("conferenceHome/"))
            path.startsWith("/sessions/") -> {
                val (conference, date) = path.substringAfter("sessions/").split("/", limit = 2)
                Config.ConferenceSessions(user, conference, date.toLocalDate())
            }
            path.startsWith("/session/") -> {
                val (conference, session) = path.substringAfter("session/").split("/", limit = 2)
                Config.SessionDetails(user, conference, session)
            }
            path.startsWith("/speaker/") -> {
                val (conference, speaker) = path.substringAfter("speaker/").split("/", limit = 2)
                Config.SpeakerDetails(user, conference, speaker)
            }
            path.startsWith("/bookmarks/") -> Config.Bookmarks(user, path.substringAfter("bookmarks/"))
            else -> null.also {
                Log.w("WearAppComponent", "Unhandled deeplink $uri")
            }
        }
    }

    private fun buildStack(target: Config): List<Config> {
        when (target) {
            is Config.ConferenceAware -> listOf()
            else -> listOf(target)
        }
        return listOf(target)
    }

    override fun handleDeeplink(intent: Intent): Boolean {
        val stack: List<Config>? = deeplinkStack(intent)

        if (stack != null) {
            navigation.replaceAll(*stack.toTypedArray())
        }

        return stack != null
    }

    private fun deeplinkStack(intent: Intent): List<Config>? {
        var stack: List<Config>? = null

        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.dataString

            if (uri != null) {
                val target = buildConfig(authentication.currentUser.value?.uid, uri)

                if (target != null) {
                    stack = buildStack(target)
                }
            }
        }
        return stack
    }
}
