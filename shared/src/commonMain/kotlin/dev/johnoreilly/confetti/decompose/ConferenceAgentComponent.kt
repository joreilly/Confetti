package dev.johnoreilly.confetti.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import ai.koog.embeddings.base.Embedder
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.agent.AgentProvider
import dev.johnoreilly.confetti.agent.ConferenceAgentProvider
import dev.johnoreilly.confetti.agent.EmbeddingCache
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ConferenceAgentComponent {
    val uiState: Value<UiState>

    fun updateInputText(text: String)
    fun sendMessage()
    fun restartChat()

    sealed interface Message {
        val text: String

        data class User(override val text: String) : Message
        data class Agent(override val text: String) : Message
        data class System(override val text: String) : Message
        data class Error(override val text: String) : Message
        data class ToolCall(override val text: String) : Message
    }

    data class UiState(
        val messages: List<Message>,
        val inputText: String = "",
        val isInputEnabled: Boolean = true,
        val isLoading: Boolean = false,
        val isChatEnded: Boolean = false,
    )
}

class DefaultConferenceAgentComponent(
    componentContext: ComponentContext,
    conference: String,
) : ConferenceAgentComponent, KoinComponent, ComponentContext by componentContext {

    private val repository: ConfettiRepository by inject()
    private val coroutineScope = coroutineScope()

    private val llModel: LLModel by inject()
    private val promptExecutor: PromptExecutor by inject()
    private val embedder: Embedder by inject()

    private val agentProvider: AgentProvider = ConferenceAgentProvider(
        repository = repository,
        conference = conference,
        llModel = llModel,
        promptExecutor = promptExecutor,
        embedder = embedder,
        embeddingCache = getKoin().getOrNull<EmbeddingCache>(),
    )

    private val state = MutableStateFlow(
        ConferenceAgentComponent.UiState(
            messages = listOf(
                ConferenceAgentComponent.Message.System(agentProvider.description),
            ),
        ),
    )

    override val uiState: Value<ConferenceAgentComponent.UiState> =
        state.asValue(lifecycle = lifecycle)

    private var pendingUserResponse: CompletableDeferred<String>? = null
    private var agentStarted: Boolean = false

    override fun updateInputText(text: String) {
        state.update { it.copy(inputText = text) }
    }

    override fun sendMessage() {
        val input = state.value.inputText.trim()
        if (input.isEmpty()) return

        val pending = pendingUserResponse
        if (pending != null) {
            state.update {
                it.copy(
                    messages = it.messages + ConferenceAgentComponent.Message.User(input),
                    inputText = "",
                    isInputEnabled = false,
                    isLoading = true,
                )
            }
            pendingUserResponse = null
            pending.complete(input)
            return
        }

        state.update {
            it.copy(
                messages = it.messages + ConferenceAgentComponent.Message.User(input),
                inputText = "",
                isInputEnabled = false,
                isLoading = true,
            )
        }

        if (!agentStarted) {
            agentStarted = true
            coroutineScope.launch {
                runAgent(input)
            }
        }
    }

    override fun restartChat() {
        pendingUserResponse?.complete("")
        pendingUserResponse = null
        agentStarted = false
        state.value = ConferenceAgentComponent.UiState(
            messages = listOf(
                ConferenceAgentComponent.Message.System(agentProvider.description),
            ),
        )
    }

    private suspend fun runAgent(initialInput: String) {
        withContext(Dispatchers.Default) {
            try {
                val agent = agentProvider.provideAgent(
                    onToolCallEvent = { message ->
                        state.update {
                            it.copy(
                                messages = it.messages +
                                    ConferenceAgentComponent.Message.ToolCall(message),
                            )
                        }
                    },
                    onErrorEvent = { errorMessage ->
                        state.update {
                            it.copy(
                                messages = it.messages +
                                    ConferenceAgentComponent.Message.Error(errorMessage),
                                isInputEnabled = true,
                                isLoading = false,
                            )
                        }
                    },
                    onAssistantMessage = { message ->
                        val deferred = CompletableDeferred<String>()
                        pendingUserResponse = deferred
                        state.update {
                            it.copy(
                                messages = it.messages +
                                    ConferenceAgentComponent.Message.Agent(message),
                                isInputEnabled = true,
                                isLoading = false,
                            )
                        }
                        deferred.await()
                    },
                )
                agent.run(initialInput)
                state.update {
                    it.copy(
                        messages = it.messages +
                            ConferenceAgentComponent.Message.System("The agent has stopped."),
                        isInputEnabled = false,
                        isLoading = false,
                        isChatEnded = true,
                    )
                }
            } catch (e: Exception) {
                state.update {
                    it.copy(
                        messages = it.messages +
                            ConferenceAgentComponent.Message.Error("Error: ${e.message}"),
                        isInputEnabled = true,
                        isLoading = false,
                    )
                }
            } finally {
                agentStarted = false
            }
        }
    }
}
