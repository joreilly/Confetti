package dev.johnoreilly.confetti.agent

import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import dev.johnoreilly.confetti.BuildKonfig

actual fun getLLModel() = GoogleModels.Gemini2_5Flash

actual fun getPromptExecutor(): PromptExecutor {
    return simpleGoogleAIExecutor(BuildKonfig.GEMINI_API_KEY)
}
