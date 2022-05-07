package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.russhwolf.settings.JvmPreferencesSettings
import com.russhwolf.settings.ObservableSettings
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import dev.johnoreilly.confetti.utils.JvmDateTimeFormatter
import org.koin.dsl.module
import java.util.prefs.Preferences

actual fun platformModule() = module {
    single<ObservableSettings> { JvmPreferencesSettings(Preferences.userRoot()) }
    single<NormalizedCacheFactory> { SqlNormalizedCacheFactory("jdbc:sqlite:confetti.db") }
    single<DateTimeFormatter> { JvmDateTimeFormatter() }
}
