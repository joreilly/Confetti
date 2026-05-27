@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.di

import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.cache.normalized.sql.SqlNormalizedCacheFactory
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import ai.koog.embeddings.base.Embedder
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import com.russhwolf.settings.coroutines.toFlowSettings
import dev.johnoreilly.confetti.BuildKonfig
import dev.johnoreilly.confetti.agent.ApiEmbedder
import dev.johnoreilly.confetti.agent.EmbeddingCache
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.dev.johnoreilly.confetti.work.SessionNotificationSender
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.JvmDateService
import dev.johnoreilly.confetti.work.NotificationSender
import okhttp3.OkHttpClient
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.util.prefs.Preferences

@OptIn(ExperimentalSettingsApi::class)
actual fun platformModule() = module {
    single<Authentication> { Authentication.Disabled }
    single<ObservableSettings> { PreferencesSettings(Preferences.userRoot()) }
    single { get<ObservableSettings>().toFlowSettings() }
    singleOf(::JvmDateService) { bind<DateService>() }
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .build()
    }
    single<FetchPolicy> {
        FetchPolicy.CacheAndNetwork
    }
    single<NotificationSender> { SessionNotificationSender() }

    System.getProperty("user.home")?.let { home ->
        single<EmbeddingCache> {
            EmbeddingCache(FileSystem.SYSTEM, "$home/.cache/confetti-embeddings".toPath())
        }
    }

    single<Embedder> {
        ApiEmbedder(
            provider = GoogleLLMClient(BuildKonfig.GEMINI_API_KEY),
            model = GoogleModels.Embeddings.GeminiEmbedding001,
        )
    }

    single<LLModel> { GoogleModels.Gemini2_5Flash }
    single<PromptExecutor> { simpleGoogleAIExecutor(BuildKonfig.GEMINI_API_KEY) }
}

actual fun getNormalizedCacheFactory(conference: String, uid: String?): NormalizedCacheFactory {
    val sqlNormalizedCacheFactory = SqlNormalizedCacheFactory("jdbc:sqlite:$conference$uid.db")
    return MemoryCacheFactory(10 * 1024 * 1024)
        .chain(sqlNormalizedCacheFactory)
}