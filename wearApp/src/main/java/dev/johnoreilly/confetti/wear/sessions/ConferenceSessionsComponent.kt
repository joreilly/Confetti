package dev.johnoreilly.confetti.wear.sessions

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.decompose.SessionsSimpleComponent
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.decompose.asValue
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface ConferenceSessionsComponent {
    val uiState: Value<SessionsUiState>

    fun onSessionClicked(session: String)
}

class DefaultConferenceSessionsComponent(
    componentContext: ComponentContext,
    conference: String,
    date: LocalDate,
    user: User?,
    private val onSessionSelected: (String) -> Unit,
) : ConferenceSessionsComponent, KoinComponent, ComponentContext by componentContext {
    private val simpleComponent =
        SessionsSimpleComponent(
            componentContext = childContext(key = "Sessions"),
            conference = conference,
            user = user,
            date = date
        )

    val repository: ConfettiRepository = get()

    override val uiState: Value<SessionsUiState> = simpleComponent.uiState.asValue(lifecycle = lifecycle)

    override fun onSessionClicked(session: String) {
        onSessionSelected(session)
    }
}
