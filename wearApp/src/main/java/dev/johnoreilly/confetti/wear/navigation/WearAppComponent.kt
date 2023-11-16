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
import com.google.android.horologist.networks.data.DataRequestRepository
import com.google.android.horologist.networks.data.DataUsageReport
import com.google.android.horologist.networks.data.Networks
import com.google.android.horologist.networks.status.NetworkRepository
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.decompose.coroutineScope
import dev.johnoreilly.confetti.wear.AppUiState
import dev.johnoreilly.confetti.wear.navigation.WearAppComponent.NetworkStatusAppState
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface WearAppComponent {
    val config: Config

    val stack: Value<ChildStack<Config, Child>>

    val appState: StateFlow<AppUiState?>
    val networkState: StateFlow<NetworkStatusAppState>

    val isWaitingOnThemeOrData: Boolean

    fun navigateUp()

    fun handleDeeplink(intent: Intent): Boolean

    fun onUserChanged(uid: String?)

    fun showConferences()

    fun showConference(conference: String)

    fun navigateTo(config: Config)
    suspend fun waitForConference(): String

    data class NetworkStatusAppState(
        val networks: Networks,
        val dataUsage: DataUsageReport? = null,
    )
}

class DefaultWearAppComponent(
    componentContext: ComponentContext,
    intent: Intent,
) : WearAppComponent, KoinComponent, ComponentContext by componentContext {
    internal val coroutineScope = coroutineScope()
    private val authentication: Authentication by inject()
    internal val repository: ConfettiRepository by inject()
    internal val navigation = StackNavigation<Config>()
    val phoneSettingsSync: PhoneSettingsSync by inject()
    private val networkRepository: NetworkRepository by inject()
    private val dataRequestRepository: DataRequestRepository by inject()

    override val appState: StateFlow<AppUiState?> = combine(
        phoneSettingsSync.settingsFlow,
        phoneSettingsSync.conferenceFlow,
        authentication.currentUser
    ) { phoneSettings, defaultConference, user ->
        AppUiState(
            defaultConference = defaultConference,
            settings = phoneSettings,
            user = user
        )
    }
        .stateIn(
            coroutineScope,
            SharingStarted.Eagerly,
            null
        )

    override val networkState =
        combine(
            networkRepository.networkStatus,
            dataRequestRepository.currentPeriodUsage(),
        ) { networkStatus, currentPeriodUsage ->
            NetworkStatusAppState(networks = networkStatus, dataUsage = currentPeriodUsage)
        }
            .stateIn(
                coroutineScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = NetworkStatusAppState(
                    networks = networkRepository.networkStatus.value,
                    dataUsage = null,
                ),
            )

    override suspend fun waitForConference(): String {
        return appState.map { it?.defaultConference }.firstOrNull() ?: AppSettings.CONFERENCE_NOT_SET
    }

    override val isWaitingOnThemeOrData: Boolean
        get() = config is Config.Loading || appState.value == null

    internal val user: User? get() = authentication.currentUser.value

    override val config: Config
        get() = stack.value.active.configuration

    override val stack: Value<ChildStack<Config, Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialStack = { initialConfig(intent) },
            childFactory = this::buildChild,
        )

    private fun initialConfig(intent: Intent) =
        (deeplinkStack(intent) ?: listOf(Config.Loading))

    init {
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
            path.startsWith("/home/") -> Config.Home(user, path.substringAfter("home/"))
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
