@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import dev.johnoreilly.confetti.ConferenceRefresh
import dev.johnoreilly.confetti.JobConferenceRefresh
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.IosDateService
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformModule() = module {
    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single { get<ObservableSettings>().toFlowSettings() }
    single<NormalizedCacheFactory> { SqlNormalizedCacheFactory("confetti.db") }
    single<DateService> { IosDateService() }
    factory {
        ApolloClient.Builder()
    }
}

actual fun getDatabaseName(conference: String) = "$conference.db"
