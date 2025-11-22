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
import com.russhwolf.settings.coroutines.toFlowSettings
import dev.johnoreilly.confetti.appconfig.ApplicationInfo
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.dev.johnoreilly.confetti.work.SessionNotificationSender
import dev.johnoreilly.confetti.prompt.PromptApi
import dev.johnoreilly.confetti.prompt.PromptApiIos
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.IosDateService
import dev.johnoreilly.confetti.work.NotificationSender
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults


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
    single<PromptApi> { PromptApiIos(get()) }

    single<ApplicationInfo> { getApplicationInfo() }
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