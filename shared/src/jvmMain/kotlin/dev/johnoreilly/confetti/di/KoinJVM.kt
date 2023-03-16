@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.network.okHttpClient
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.JvmDateService
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.util.prefs.Preferences
import com.russhwolf.settings.coroutines.toFlowSettings

actual fun platformModule() = module {
    single<ObservableSettings> { PreferencesSettings(Preferences.userRoot()) }
    single { get<ObservableSettings>().toFlowSettings() }
    single<DateService> { JvmDateService() }
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .build()
    }
    factory {
        ApolloClient.Builder().okHttpClient(get())
    }
}

actual fun getDatabaseName(conference: String) = "jdbc:sqlite:$conference.db"
