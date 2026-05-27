package dev.johnoreilly.confetti.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.embeddings.base.Embedder
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import dev.johnoreilly.confetti.ConfettiRepository
import kotlin.time.ExperimentalTime

data class EmbeddingCache(val fs: okio.FileSystem, val root: okio.Path)

class ConferenceAgentProvider(
    private val repository: ConfettiRepository,
    private val conference: String,
    private val llModel: LLModel,
    private val promptExecutor: PromptExecutor,
    private val embedder: Embedder,
    private val embeddingCache: EmbeddingCache?,
) : AgentProvider {

    override val description: String =
        "Hi, I'm a conference assistant. Ask me about sessions, speakers and topics at this conference."

    @OptIn(ExperimentalTime::class)
    override suspend fun provideAgent(
        onToolCallEvent: suspend (String) -> Unit,
        onErrorEvent: suspend (String) -> Unit,
        onAssistantMessage: suspend (String) -> String,
    ): AIAgent<String, String> {

        val sessionIndex = SessionEmbeddingIndex(
            repository = repository,
            conference = conference,
            embedder = embedder,
            cache = embeddingCache,
        )

        val toolRegistry = ToolRegistry {
            tool(GetSessionsTool(repository, conference))
            tool(SearchSessionsTool(sessionIndex))
            tool(GetSessionByIdTool(repository, conference))
            tool(GetSpeakersTool(repository, conference))
            tool(GetSpeakerByIdTool(repository, conference))
        }

        val agentConfig = AIAgentConfig(
            prompt = prompt("confetti") {
                system(
                    """
                    You are an AI assistant for the "$conference" conference.
                    Help the user find sessions and speakers that match their interests.
                    Use the provided tools to look up sessions and speakers — do not invent ids,
                    titles or any other details. When recommending sessions, reference them by
                    title and include their speakers, start time and room when available.

                    Choosing between session tools:
                    - Default to SearchSessionsTool whenever the user asks about a topic,
                      theme, or area of interest — including single-word topics like "AI",
                      "testing", "UI" or "performance". Semantic search finds related talks
                      even when they use different vocabulary.
                    - Each SearchSessionsTool result includes a similarity score. Treat
                      scores above ~0.5 as strong matches; consider including borderline
                      results (down to ~0.3) when the user asks for "all" related talks.
                    - Only use GetSessionsTool when the user explicitly wants a verbatim
                      string match (e.g. "find talks with 'Kotlin Multiplatform' in the
                      title").
                    """.trimIndent(),
                )
            },
            model = llModel,
            maxAgentIterations = 20,
        )

        return AIAgent(
            promptExecutor = promptExecutor,
            strategy = createStrategy(onAssistantMessage),
            agentConfig = agentConfig,
            toolRegistry = toolRegistry,
        ) {
            install(EventHandler) {
                onToolCallStarting { ctx ->
                    onToolCallEvent("Tool ${ctx.toolName}, args ${ctx.toolArgs}")
                }
                onAgentExecutionFailed { ctx ->
                    onErrorEvent(ctx.error.message ?: "Unknown error")
                }
            }
        }
    }

    private fun createStrategy(onAssistantMessage: suspend (String) -> String) =
        functionalStrategy<String, String> { initialInput ->
            var inputMessage = initialInput
            var assistantMessage = ""

            while (inputMessage.isNotEmpty()) {
                var response = requestLLM(inputMessage)

                while (getToolCalls(response).isNotEmpty()) {
                    val results = executeTools(response)
                    response = sendToolResults(results)
                }

                assistantMessage = response.textContent()
                inputMessage = onAssistantMessage(assistantMessage)
            }
            assistantMessage
        }
}
