package dev.johnoreilly.confetti.agent

import ai.koog.agents.core.agent.AIAgent

interface AgentProvider {
    val description: String

    suspend fun provideAgent(
        onToolCallEvent: suspend (String) -> Unit,
        onErrorEvent: suspend (String) -> Unit,
        onAssistantMessage: suspend (String) -> String,
    ): AIAgent<String, String>
}
