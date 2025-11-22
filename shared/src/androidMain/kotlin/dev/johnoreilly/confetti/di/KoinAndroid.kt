@file:OptIn(
    ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class,
    ExperimentalHorologistApi::class
)
@file:Suppress("RemoveExplicitTypeArguments")

package dev.johnoreilly.confetti.di

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkManager
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.ApolloClientAwarenessInterceptor
import com.apollographql.apollo.network.http.DefaultHttpEngine
import com.apollographql.apollo.network.ws.DefaultWebSocketEngine
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.cache.normalized.sql.SqlNormalizedCacheFactory
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.data.WearDataLayerRegistry
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import dev.johnoreilly.confetti.ai.OnDeviceAI
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.AndroidLoggingAnalyticsLogger
import dev.johnoreilly.confetti.analytics.FirebaseAnalyticsLogger
import dev.johnoreilly.confetti.appconfig.ApplicationInfo
import dev.johnoreilly.confetti.prompt.PromptApi
import dev.johnoreilly.confetti.prompt.PromptApiAndroid
import dev.johnoreilly.confetti.settings.WearSettingsSerializer
import dev.johnoreilly.confetti.shared.BuildConfig
import dev.johnoreilly.confetti.utils.AndroidDateService
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.work.NotificationSender
import dev.johnoreilly.confetti.work.RefreshWorker
import dev.johnoreilly.confetti.work.SessionNotificationSender
import dev.johnoreilly.confetti.work.SessionNotificationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

@OptIn(ExperimentalHorologistApi::class, ExperimentalSettingsImplementation::class)
actual fun platformModule() = module {
    singleOf(::AndroidDateService) { bind<DateService>() }
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .apply {
//                if (BuildConfig.DEBUG) {
//                    eventListenerFactory(LoggingEventListener.Factory())
//                }
            }
            .build()
    }
    single<FetchPolicy> {
        FetchPolicy.CacheAndNetwork
    }
    single<WebSocket.Factory> {
        get<OkHttpClient>()
    }
    factory<ApolloClient.Builder> {
        ApolloClient.Builder()
            .serverUrl("https://confetti-app.dev/graphql")
            .httpEngine(DefaultHttpEngine(get<Call.Factory>(named("API"))))
            .addHttpInterceptor(ApolloClientAwarenessInterceptor("confetti-android", androidContext().versionCode))
            .webSocketEngine(DefaultWebSocketEngine(get<WebSocket.Factory>()))
    }
    single<Call.Factory>(qualifier = named("API")) {
        get<OkHttpClient>()
    }
    single<Call.Factory>(qualifier = named("images")) {
        get<OkHttpClient>()
    }
    single<Call.Factory>(qualifier = named("logs")) {
        get<OkHttpClient>()
    }
    single<ImageLoader> {
        ImageLoader.Builder(androidContext())
            .callFactory { get(named("images")) }
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
    single<AnalyticsLogger> {
        if (BuildConfig.DEBUG) {
            AndroidLoggingAnalyticsLogger
        } else {
            FirebaseAnalyticsLogger
        }
    }
    single { androidContext().settingsStore }
    single { NotificationManagerCompat.from(androidContext()) }
    singleOf(::DataStoreSettings) { bind<FlowSettings>() }
    workerOf(::RefreshWorker)
    workerOf(::SessionNotificationWorker)
    singleOf(::SessionNotificationSender)
    single { WorkManager.getInstance(androidContext()) }

    single<CoroutineScope> { CoroutineScope(Dispatchers.Default) }

    single<WearDataLayerRegistry> {
        WearDataLayerRegistry.fromContext(
            application = androidContext(),
            coroutineScope = get()
        ).apply {
            registerSerializer(WearSettingsSerializer)
        }
    }
    single<NotificationSender> {
        get<SessionNotificationSender>()
    }
    singleOf(::OnDeviceAI)
    single<PromptApi> { PromptApiAndroid(get()) }

    single<ApplicationInfo> { getApplicationInfo(get()) }
}

val Context.settingsStore by preferencesDataStore("settings")


actual fun getNormalizedCacheFactory(conference: String, uid: String?): NormalizedCacheFactory {
    val sqlNormalizedCacheFactory = SqlNormalizedCacheFactory("$conference$uid.db")
    return MemoryCacheFactory(10 * 1024 * 1024)
        .chain(sqlNormalizedCacheFactory)
}

private val Context.versionCode: String
    get() {
        return packageManager.getPackageInfo(packageName, 0).let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                it.longVersionCode
            } else {
                it.versionCode
            }
        }.toString()
    }


private fun getApplicationInfo(application: Application): ApplicationInfo {
    val packageManager = application.packageManager
    val versionName = packageManager.getPackageInfo(application.packageName, 0).versionName ?: ""
    return ApplicationInfo(versionName)
}