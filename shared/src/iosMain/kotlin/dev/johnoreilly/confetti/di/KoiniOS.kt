package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.russhwolf.settings.AppleSettings
import com.russhwolf.settings.ObservableSettings
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import dev.johnoreilly.confetti.utils.IosDateTimeFormatter
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformModule() = module {
    single<ObservableSettings> { AppleSettings(NSUserDefaults.standardUserDefaults) }
    single<NormalizedCacheFactory> { SqlNormalizedCacheFactory("confetti.db") }
    single<DateTimeFormatter> { IosDateTimeFormatter() }
}
