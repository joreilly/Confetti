package dev.johnoreilly.confetti.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import dev.johnoreilly.confetti.ConfettiRepository
import kotlin.time.ExperimentalTime

expect fun getLLModel(): LLModel
expect fun getPromptExecutor(): PromptExecutor

class ConferenceAgentProvider(
    private val repository: ConfettiRepository,
    private val conference: String,
) : AgentProvider {

    override val description: String =
        "Hi, I'm a conference assistant. Ask me about sessions, speakers and topics at this conference."

    @OptIn(ExperimentalTime::class)
    override suspend fun provideAgent(
        onToolCallEvent: suspend (String) -> Unit,
        onErrorEvent: suspend (String) -> Unit,
        onAssistantMessage: suspend (String) -> String,
    ): AIAgent<String, String> {

        val toolRegistry = ToolRegistry {
            tool(GetSessionsTool(repository, conference))
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
                    title and include their start time and room when available.
                    """.trimIndent(),
                )
            },
            model = getLLModel(),
            maxAgentIterations = 20,
        )

        return AIAgent(
            promptExecutor = getPromptExecutor(),
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
