@file:OptIn(ExperimentalSettingsApi::class, ApolloExperimental::class)

package dev.johnoreilly.confetti.di

import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import ai.koog.embeddings.base.Embedder
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import com.russhwolf.settings.observable.makeObservable
import dev.johnoreilly.confetti.BuildKonfig
import dev.johnoreilly.confetti.agent.ApiEmbedder
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.dev.johnoreilly.confetti.work.SessionNotificationSender
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.WasmDateService
import dev.johnoreilly.confetti.work.NotificationSender
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
actual fun platformModule() = module {
    single<Authentication> { Authentication.Disabled }
    single<FlowSettings> {  StorageSettings().makeObservable().toFlowSettings() }
    singleOf(::WasmDateService) { bind<DateService>() }
    single<FetchPolicy> {
        FetchPolicy.CacheAndNetwork
    }
    single<NotificationSender> { SessionNotificationSender() }

    single<Embedder> {
        ApiEmbedder(
            provider = GoogleLLMClient(
                apiKey = BuildKonfig.GEMINI_API_KEY,
                httpClientFactory = KtorKoogHttpClient.Factory(),
            ),
            model = GoogleModels.Embeddings.GeminiEmbedding001,
        )
    }

    single<LLModel> { GoogleModels.Gemini2_5Flash }
    single<PromptExecutor> {
        simpleGoogleAIExecutor(BuildKonfig.GEMINI_API_KEY, KtorKoogHttpClient.Factory())
    }
}

actual fun getNormalizedCacheFactory(conference: String, uid: String?): NormalizedCacheFactory {
    return MemoryCacheFactory(10 * 1024 * 1024)
}