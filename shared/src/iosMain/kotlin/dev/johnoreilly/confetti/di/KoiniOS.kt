@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.ApolloClientAwarenessInterceptor
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.cache.normalized.sql.SqlNormalizedCacheFactory
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import ai.koog.embeddings.base.Embedder
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import com.russhwolf.settings.coroutines.toFlowSettings
import dev.johnoreilly.confetti.BuildKonfig
import dev.johnoreilly.confetti.agent.ApiEmbedder
import dev.johnoreilly.confetti.agent.EmbeddingCache
import dev.johnoreilly.confetti.appconfig.ApplicationInfo
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.dev.johnoreilly.confetti.work.SessionNotificationSender
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.IosDateService
import dev.johnoreilly.confetti.work.NotificationSender
import okio.FileSystem
import okio.Path.Companion.toPath
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask


@OptIn(ExperimentalSettingsApi::class)
actual fun platformModule() = module {
    single<Authentication> { Authentication.Disabled }
    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single { get<ObservableSettings>().toFlowSettings() }
    singleOf(::IosDateService) { bind<DateService>() }
    single<FetchPolicy> { FetchPolicy.CacheAndNetwork }
    factory {
        ApolloClient.Builder()
            .serverUrl("https://confetti-app.dev/graphql")
            .addHttpInterceptor(ApolloClientAwarenessInterceptor("confetti-ios", "fixme"))
    }
    single<NotificationSender> { SessionNotificationSender() }

    single<ApplicationInfo> { getApplicationInfo() }

    embeddingCache()?.let { cache -> single<EmbeddingCache> { cache } }

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

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
private fun embeddingCache(): EmbeddingCache? {
    val url = NSFileManager.defaultManager.URLsForDirectory(
        directory = NSCachesDirectory,
        inDomains = NSUserDomainMask,
    ).firstOrNull() as? NSURL ?: return null
    val path = url.path ?: return null
    return EmbeddingCache(FileSystem.SYSTEM, "$path/confetti-embeddings".toPath())
}

actual fun getNormalizedCacheFactory(conference: String, uid: String?): NormalizedCacheFactory {
    val sqlNormalizedCacheFactory = SqlNormalizedCacheFactory("$conference$uid.db")
    return MemoryCacheFactory(10 * 1024 * 1024)
        .chain(sqlNormalizedCacheFactory)
}

private fun getApplicationInfo(): ApplicationInfo {
    val versionName = NSBundle.mainBundle.infoDictionary
        ?.get("CFBundleShortVersionString") as? String ?: ""
    return ApplicationInfo(versionName)
}