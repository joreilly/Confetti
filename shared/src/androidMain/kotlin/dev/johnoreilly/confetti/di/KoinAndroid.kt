package dev.johnoreilly.confetti.di

import android.content.Context
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.russhwolf.settings.AndroidSettings
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import dev.johnoreilly.confetti.utils.AndroidDateTimeFormatter
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
actual fun platformModule() = module {
    single<ObservableSettings> { createObservableSettings(get()) }
    single<NormalizedCacheFactory> { SqlNormalizedCacheFactory(get(), "confetti.db") }
    single<DateTimeFormatter> { AndroidDateTimeFormatter() }
}


@OptIn(ExperimentalSettingsApi::class)
private fun createObservableSettings(context: Context): ObservableSettings {
    return AndroidSettings(context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
}