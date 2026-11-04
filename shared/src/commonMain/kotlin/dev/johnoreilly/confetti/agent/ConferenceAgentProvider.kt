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
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
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
    // Semantic search is only on when embeddings are computed on-device, so it never
    // makes Gemini embedding API calls. Pass true to enable it with a remote embedder.
    private val embeddingsEnabled: Boolean = embedder is OnDeviceEmbedder,
) : AgentProvider {

    override val description: String =
        "Hi, I'm a conference assistant. Ask me about sessions, speakers and topics at this conference."

    @OptIn(ExperimentalTime::class)
    override suspend fun provideAgent(
        onToolCallEvent: suspend (String) -> Unit,
        onErrorEvent: suspend (String) -> Unit,
        onAssistantMessage: suspend (String) -> String,
    ): AIAgent<String, String> {

        val sessionIndex = if (embeddingsEnabled) {
            SessionEmbeddingIndex(
                repository = repository,
                conference = conference,
                embedder = embedder,
                cache = embeddingCache,
            )
        } else {
            null
        }

        // Small (on-device) context windows need compact tool output and pruned history.
        val smallContext = (llModel.contextLength ?: Long.MAX_VALUE) <= 8_192
        val toolLimits = if (smallContext) ToolOutputLimits.Compact else ToolOutputLimits()
        val toolRegistry = ToolRegistry {
            tool(GetSessionsTool(repository, conference, toolLimits))
            if (sessionIndex != null) {
                tool(SearchSessionsTool(sessionIndex, toolLimits))
            }
            tool(GetSessionByIdTool(repository, conference))
            tool(GetSpeakersTool(repository, conference, toolLimits))
            tool(GetSpeakerByIdTool(repository, conference))
        }

        val sessionToolGuidance = if (embeddingsEnabled) {
            """

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
            """.trimIndent()
        } else {
            """

            Use GetSessionsTool to find sessions by a word or phrase in their title
            or description. Try a few relevant keywords when the user asks about a
            topic or theme.
            """.trimIndent()
        }

        // On-device models generate slowly, so long answers dominate response time.
        val briefAnswerGuidance = if (smallContext) {
            "\n\n" + """
            Keep answers brief: list each session on one line as its title, speakers and
            start time. Do not repeat session descriptions unless the user asks for them.
            """.trimIndent()
        } else {
            ""
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
                    """.trimIndent() + "\n" + sessionToolGuidance + briefAnswerGuidance,
                )
            },
            model = llModel,
            maxAgentIterations = 20,
        )

        return AIAgent(
            promptExecutor = promptExecutor,
            strategy = createStrategy(onAssistantMessage, pruneHistory = smallContext),
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

    private fun createStrategy(
        onAssistantMessage: suspend (String) -> String,
        pruneHistory: Boolean,
    ) =
        functionalStrategy<String, String> { initialInput ->
            var inputMessage = initialInput
            var assistantMessage = ""

            while (inputMessage.isNotEmpty()) {
                if (pruneHistory) {
                    llm.writeSession { prompt = prompt.withMessages(::pruneForSmallContext) }
                }
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

/**
 * Keeps a conversation within a small context window between turns: drops earlier turns'
 * tool calls and results (the answers that used them are kept), then the oldest exchanges
 * until the remaining history fits a rough character budget.
 */
private fun pruneForSmallContext(messages: List<Message>): List<Message> {
    val (system, history) = messages
        .filterNot { message -> message.parts.isNotEmpty() && message.parts.all { it is MessagePart.Tool } }
        .partition { it is Message.System }
    val trimmed = history.toMutableList()
    // ~4 chars per token; leaves room for the system prompt, tool schemas and the next turn.
    while (trimmed.size > 1 && trimmed.sumOf { it.textContent().length } > 6_000) {
        trimmed.removeAt(0)
    }
    return system + trimmed.dropWhile { it !is Message.User }
}
