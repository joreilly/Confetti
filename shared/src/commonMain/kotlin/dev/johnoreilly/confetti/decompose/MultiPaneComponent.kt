package dev.johnoreilly.confetti.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.johnoreilly.confetti.auth.User

interface MultiPaneComponent {

    val sessions: SessionsComponent
    val sessionDetails: Value<ChildSlot<*, SessionDetailsComponent>>
}

class DefaultMultiPaneComponent(
    componentContext: ComponentContext,
    conference: String,
    user: User?,
    onSignIn: () -> Unit,
    onSpeakerSelected: (id: String) -> Unit,
) : MultiPaneComponent, ComponentContext by componentContext {

    private val sessionDetailsNavigation = SlotNavigation<SessionDetailsConfig>()

    override val sessions: SessionsComponent =
        DefaultSessionsComponent(
            componentContext = childContext(key = "sessions"),
            conference = conference,
            user = user,
            onSessionSelected = { id ->
                sessionDetailsNavigation.activate(SessionDetailsConfig(sessionId = id))
            },
            onSignIn = onSignIn,
        )

    private val _sessionDetails: Value<ChildSlot<SessionDetailsConfig, SessionDetailsComponent>> =
        childSlot(source = sessionDetailsNavigation) { config, childComponentContext ->
            DefaultSessionDetailsComponent(
                componentContext = childComponentContext,
                conference = conference,
                sessionId = config.sessionId,
                user = user,
                onFinished = sessionDetailsNavigation::dismiss,
                onSignIn = onSignIn,
                onSpeakerSelected = onSpeakerSelected,
            )
        }

    override val sessionDetails: Value<ChildSlot<*, SessionDetailsComponent>> = _sessionDetails

    init {
        _sessionDetails.subscribe {
            sessions.onSessionSelectionChanged(id = it.child?.configuration?.sessionId)
        }
    }

    @Parcelize
    private data class SessionDetailsConfig(
        val sessionId: String,
    ) : Parcelable
}
