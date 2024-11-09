package dev.johnoreilly.confetti.wear.sessions

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.decompose.SessionsSimpleComponent
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.decompose.asValue
import dev.johnoreilly.confetti.decompose.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Duration.Companion.seconds

interface ConferenceSessionsComponent {
    val uiState: Value<SessionsUiState>

    fun onSessionClicked(session: String)

    fun addBookmark(sessionId: String)
    fun removeBookmark(sessionId: String)
}

class DefaultConferenceSessionsComponent(
    componentContext: ComponentContext,
    private val conference: String,
    date: LocalDate,
    private val user: User?,
    private val onSessionSelected: (String) -> Unit,
) : ConferenceSessionsComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()

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

    override fun addBookmark(sessionId: String) {
        coroutineScope.launch {
            repository.addBookmark(conference, user?.uid, user, sessionId)
        }
    }

    override fun removeBookmark(sessionId: String) {
        coroutineScope.launch {
            repository.removeBookmark(conference, user?.uid, user, sessionId)
        }
    }
}
