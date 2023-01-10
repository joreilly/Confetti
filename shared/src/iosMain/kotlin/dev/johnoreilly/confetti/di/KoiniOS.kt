package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import dev.johnoreilly.confetti.utils.IosDateTimeFormatter
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformModule() = module {
    single<ObservableSettings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single<NormalizedCacheFactory> { SqlNormalizedCacheFactory("confetti.db") }
    single<DateTimeFormatter> { IosDateTimeFormatter() }
    factory {
        ApolloClient.Builder()
    }
}

actual fun getDatabaseName(conference: String) = "$conference.db"
