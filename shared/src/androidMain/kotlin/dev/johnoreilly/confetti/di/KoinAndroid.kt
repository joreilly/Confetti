package dev.johnoreilly.confetti.di

import android.content.Context
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.johnoreilly.confetti.utils.AndroidDateTimeFormatter
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import org.koin.dsl.module

actual fun platformModule() = module {
    single<ObservableSettings> { createObservableSettings(get()) }
    single<NormalizedCacheFactory> { SqlNormalizedCacheFactory(get(), "confetti.db") }
    single<DateTimeFormatter> { AndroidDateTimeFormatter() }
}


private fun createObservableSettings(context: Context): ObservableSettings {
    return SharedPreferencesSettings(context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
}