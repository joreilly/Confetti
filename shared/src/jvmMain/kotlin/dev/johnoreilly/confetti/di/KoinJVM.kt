package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import dev.johnoreilly.confetti.utils.DateTimeFormatter
import dev.johnoreilly.confetti.utils.JvmDateTimeFormatter
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.util.prefs.Preferences

actual fun platformModule() = module {
    single<ObservableSettings> { PreferencesSettings(Preferences.userRoot()) }
    single<DateTimeFormatter> { JvmDateTimeFormatter() }
    single<OkHttpClient> {
        OkHttpClient.Builder()
            // Happy eyeballs
            .fastFallback(true)
            .build()
    }
    factory {
        ApolloClient.Builder().okHttpClient(get())
    }
}

actual fun getDatabaseName(conference: String) = "jdbc:sqlite:$conference.db"
