package dev.johnoreilly.confetti.di

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import dev.johnoreilly.confetti.utils.JvmDateTimeFormatter
import org.koin.dsl.module
import java.util.prefs.Preferences

actual fun platformModule() = module {
    single<ObservableSettings> { PreferencesSettings(Preferences.userRoot()) }
    single<DateTimeFormatter> { JvmDateTimeFormatter() }
}

actual fun getDatabaseName(conference: String) = "jdbc:sqlite:$conference.db"