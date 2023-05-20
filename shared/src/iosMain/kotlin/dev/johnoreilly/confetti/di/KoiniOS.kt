@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.DefaultAuthentication
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.IosDateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults
import platform.posix.bind

actual fun platformModule() = module {
    single<Authentication> { Authentication.Disabled }
    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single { get<ObservableSettings>().toFlowSettings() }
    single<NormalizedCacheFactory> { SqlNormalizedCacheFactory("confetti.db") }
    singleOf(::IosDateService) { bind<DateService>() }
    factory {
        ApolloClient.Builder()
            .serverUrl("https://confetti-app.dev/graphql")
    }
}

actual fun getDatabaseName(conference: String, uid: String?) = "$conference$uid.db"
