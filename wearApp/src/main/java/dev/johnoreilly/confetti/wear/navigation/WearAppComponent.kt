package dev.johnoreilly.confetti.wear.navigation

import android.content.Intent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface WearAppComponent {
    val stack: Value<ChildStack<Config, Child>>

    fun navigateUp()

    fun handleDeeplink(intent: Intent)

    fun onUserChanged(uid: String?)

    fun showConferences()

    fun showConference(conference: String)
}

class DefaultWearAppComponent(
    componentContext: ComponentContext,
) : WearAppComponent, KoinComponent, ComponentContext by componentContext {

    internal val coroutineScope = coroutineScope()
    private val authentication: Authentication by inject()
    internal val repository: ConfettiRepository by inject()
    internal val navigation = StackNavigation<Config>()

    internal val user: User? get() = authentication.currentUser.value

    override val stack: Value<ChildStack<Config, Child>> =
        childStack(
            source = navigation,
            initialConfiguration = Config.Loading,
            childFactory = this::buildChild,
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

    override fun handleDeeplink(intent: Intent) {
        println("TODO handleDeeplink $intent")
    }
}
