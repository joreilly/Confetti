package dev.johnoreilly.confetti.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GeminiApi
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface RecommendationsComponent {
    val uiState: Value<UiState>

    val isLoggedIn: Boolean
    val sessions: Flow<List<SessionDetails>>
    val bookmarks: Flow<Set<String>>
    val speakers: Flow<List<SpeakerDetails>>

    fun makeQuery(query: String)
    fun addBookmark(sessionId: String)
    fun removeBookmark(sessionId: String)
    fun onSessionClicked(id: String)

    sealed interface UiState
    data object Initial : UiState
    data object Loading : UiState
    data class Error(val message: String) : UiState
    data class Success(val data: RecommendationsInfo) : UiState
}

class DefaultRecommendationsComponent(
    componentContext: ComponentContext,
    private val conference: String,
    private val user: User?,
    private val onSessionSelected: (id: String) -> Unit,
) : RecommendationsComponent, KoinComponent, ComponentContext by componentContext {

    private val repository: ConfettiRepository by inject()
    private val geminiApi: GeminiApi by inject()
    private val coroutineScope = coroutineScope()

    private val uiStateStateFlow = MutableStateFlow<RecommendationsComponent.UiState>(RecommendationsComponent.Initial)

    override val uiState = uiStateStateFlow.asValue(initialValue = RecommendationsComponent.Loading, lifecycle = lifecycle)
    override val isLoggedIn: Boolean = user != null

    private val sessionsComponent =
        SessionsSimpleComponent(
            componentContext = childContext("Sessions"),
            conference = conference,
            user = user,
        )

    private val speakersComponent =
        SpeakersSimpleComponent(
            componentContext = childContext("Speakers"),
            conference = conference,
            repository = repository,
        )

    private val successSessions = sessionsComponent
        .uiState
        .filterIsInstance<SessionsUiState.Success>()

    private val successSpeakers = speakersComponent
        .speakers
        .filterIsInstance<SpeakersUiState.Success>()

    override val sessions: Flow<List<SessionDetails>> = successSessions
        .map { state ->
            state
                .sessionsByStartTimeList
                .flatMap { sessions -> sessions.values }
                .flatten()
        }

    private val sessionIdList = MutableStateFlow<List<String>>(emptyList())

    override val bookmarks: Flow<Set<String>> = successSessions
        .map { state -> state.bookmarks }

    override val speakers: Flow<List<SpeakerDetails>> = successSpeakers
        .map { state -> state.speakers }


    override fun makeQuery(query: String) {
        coroutineScope.launch {
            val sessions = sessions.first()
            var prompt =
                """
                You help selecting sessions at a conference based on the interest of the user.
                The user will give you a query and you will select the most appropriate session. 
                Output only the session id separated by a comma. 
                Output up to 10 sessions.
                
                This is the list of sessions:
                
                """

            sessions.forEach { session ->
                session.sessionDescription?.let {
                    prompt += "Session Id: ${session.id}\n"
                    prompt += "Title: ${session.title}\n"
                    prompt += "Description: ${session.sessionDescription}\n"
                    prompt += "\n"
                }
            }
            println("Prompt = $prompt")

            uiStateStateFlow.value = RecommendationsComponent.Loading
            try {
                val response = geminiApi.generateContent(prompt, query)
                response.text?.let { queryResponse ->
                    uiStateStateFlow.value = RecommendationsComponent.Success(RecommendationsInfo(queryResponse, emptyList()))

                    sessionIdList.value = queryResponse.split(",", " ")
                    println(sessionIdList.value)

                    val recommendedSessions = sessions.filter { it.id in sessionIdList.value  }
                    uiStateStateFlow.value = RecommendationsComponent.Success(RecommendationsInfo(queryResponse, recommendedSessions))
                }
            } catch (e: Exception) {
                uiStateStateFlow.value = RecommendationsComponent.Error(e.message ?: "Unknown error")
            }
        }
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

    override fun onSessionClicked(id: String) {
        onSessionSelected(id)
    }
}

data class RecommendationsInfo(
    val response: String,
    val recommendedSessions: List<SessionDetails>
)
