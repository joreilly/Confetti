@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.JvmDateService
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import java.util.prefs.Preferences

actual fun platformModule() = module {
    single<ObservableSettings> { PreferencesSettings(Preferences.userRoot()) }
    single { get<ObservableSettings>().toFlowSettings() }
    singleOf(::JvmDateService).withOptions { bind<DateService>() }
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .build()
    }
    factory {
        ApolloClient.Builder().okHttpClient(get())
    }
}

actual fun getDatabaseName(conference: String) = "jdbc:sqlite:$conference.db"
