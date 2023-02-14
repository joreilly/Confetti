@file:OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)

package dev.johnoreilly.confetti.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import coil.ImageLoader
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toBlockingObservableSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import dev.johnoreilly.confetti.analytics.AndroidLoggingAnalyticsLogger
import dev.johnoreilly.confetti.analytics.FirebaseAnalyticsLogger
import dev.johnoreilly.confetti.shared.BuildConfig
import dev.johnoreilly.confetti.utils.AndroidDateService
import dev.johnoreilly.confetti.utils.DateService
import okhttp3.OkHttpClient
import okhttp3.logging.LoggingEventListener
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() = module {
    single<DateService> { AndroidDateService() }
    single {
        OkHttpClient.Builder()
            .apply {
                // TODO enable based on debug flag
                eventListenerFactory(LoggingEventListener.Factory())
            }
            .build()
    }
    factory { ApolloClient.Builder().okHttpClient(get()) }
    single {
        ImageLoader.Builder(androidContext())
            .okHttpClient { get() }
            .build()
    }
    single {
        if (BuildConfig.DEBUG) {
            AndroidLoggingAnalyticsLogger
        } else {
            FirebaseAnalyticsLogger
        }
    }
    single { androidContext().settingsStore }
    single<FlowSettings> { DataStoreSettings(get()) }
    single {
        get<FlowSettings>().toBlockingObservableSettings()
//        SharedPreferencesSettings(
//            get<Context>().getSharedPreferences(
//                "AppSettings",
//                Context.MODE_PRIVATE
//            )
//        )
    }
}

val Context.settingsStore by preferencesDataStore("settings")

actual fun getDatabaseName(conference: String) = "$conference.db"
