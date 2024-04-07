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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface RecommendationsComponent {
    val uiState: Value<UiState>

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

    private val uiState2 = MutableStateFlow<RecommendationsComponent.UiState>(RecommendationsComponent.Initial)

    override val uiState = uiState2.asValue(initialValue = RecommendationsComponent.Loading, lifecycle = lifecycle)

    private val sessionsComponent =
        SessionsSimpleComponent(
            componentContext = childContext("Sessions"),
            conference = conference,
            user = null, //user,
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
            sessions.collect { sessions ->
                var sessionsInfo = "Speakers,Session ID,Title,\n"
                sessions.forEach { session ->
                    session.sessionDescription?.let {
                        val speakers = session.speakers.joinToString(" ") { it.speakerDetails.name }
                        sessionsInfo += "$speakers, ${session.id}, ${session.title}, \n"
                    }
                }

                val basePrompt =
                    """    
                    I would like to learn about $query. Which talks should I attend? 
                    Show me the session ids in the result as comma delimited list. 
                    Do not use markdown in the result.
                    """

                val prompt = " $basePrompt Base on the following CSV: $sessionsInfo}"

                var queryResponse = ""
                uiState2.value = RecommendationsComponent.Loading
                geminiApi.generateContent(prompt)
                    .catch {
                        uiState2.value = RecommendationsComponent.Error(it.message ?: "Error making gemini request")
                    }
                    .collect {
                        queryResponse += it.text
                        uiState2.value = RecommendationsComponent.Success(RecommendationsInfo(queryResponse, emptyList()))
                    }


                sessionIdList.value = queryResponse.split(",", " ")
                println(sessionIdList.value)

                val recommendedSessions = sessions.filter { it.id in sessionIdList.value  }
                uiState2.value = RecommendationsComponent.Success(RecommendationsInfo(queryResponse, recommendedSessions))
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
