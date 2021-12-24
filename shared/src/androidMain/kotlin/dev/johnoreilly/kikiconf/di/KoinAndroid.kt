package dev.johnoreilly.kikiconf.di

import android.content.Context
import com.russhwolf.settings.AndroidSettings
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
actual fun platformModule() = module {
    single<ObservableSettings> { createObservableSettings(get()) }
}


@OptIn(ExperimentalSettingsApi::class)
private fun createObservableSettings(context: Context): ObservableSettings {
    return AndroidSettings(context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
}