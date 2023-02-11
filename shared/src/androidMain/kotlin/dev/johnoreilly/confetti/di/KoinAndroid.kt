package dev.johnoreilly.confetti.di

import android.content.Context
import coil.ImageLoader
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
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
    single<ObservableSettings> { createObservableSettings(get()) }
    single<DateService> { AndroidDateService() }
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .apply {
                // TODO enable based on debug flag
                eventListenerFactory(LoggingEventListener.Factory())
            }
            .build()
    }
    factory {
        ApolloClient.Builder().okHttpClient(get())
    }
    single {
        ImageLoader.Builder(androidContext())
            .okHttpClient { get() }
            .build()
    }
    single<AnalyticsLogger> {
        if (BuildConfig.DEBUG) {
            AndroidLoggingAnalyticsLogger
        } else {
            FirebaseAnalyticsLogger
        }
    }
}


private fun createObservableSettings(context: Context): ObservableSettings {
    return SharedPreferencesSettings(context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
}

actual fun getDatabaseName(conference: String) = "$conference.db"
