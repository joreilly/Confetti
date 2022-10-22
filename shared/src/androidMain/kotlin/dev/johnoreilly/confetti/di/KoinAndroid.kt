package dev.johnoreilly.confetti.di

import android.content.Context
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.johnoreilly.confetti.utils.AndroidDateTimeFormatter
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import org.koin.dsl.module

actual fun platformModule() = module {
    single<ObservableSettings> { createObservableSettings(get()) }
    single<DateTimeFormatter> { AndroidDateTimeFormatter() }
}


private fun createObservableSettings(context: Context): ObservableSettings {
    return SharedPreferencesSettings(context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
}

actual fun getDatabaseName(conference: String) = "$conference.db"
