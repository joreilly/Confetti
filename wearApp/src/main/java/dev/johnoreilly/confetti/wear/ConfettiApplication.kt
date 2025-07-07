@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear

import android.app.Application
import android.content.Context
import androidx.wear.phone.interactions.notifications.BridgingConfig
import androidx.wear.phone.interactions.notifications.BridgingManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.networks.data.RequestType
import com.google.android.horologist.networks.okhttp.impl.RequestTypeHolder.Companion.withDefaultRequestType
import com.google.android.horologist.networks.okhttp.urlconnection.FirebaseUrlFactory
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.wear.di.appModule
import dev.johnoreilly.confetti.work.setupDailyRefresh
import okhttp3.Call
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import java.net.URL

class ConfettiApplication : Application(), ImageLoaderFactory {

    private val isFirebaseInstalled
        get() = try {
            FirebaseApp.getInstance()
            true
        } catch (_: IllegalStateException) {
            false
        }

    override fun newImageLoader(): ImageLoader = get()

    override fun onCreate() {
        super.onCreate()

        BridgingManager.fromContext(this).setConfig(
            BridgingConfig.Builder(this, /* isBridgingEnabled = */ true)
                .build()
        )

        if (isFirebaseInstalled) {
            if (!BuildConfig.DEBUG) {
                Firebase.crashlytics.isCrashlyticsCollectionEnabled = true
                Firebase.crashlytics.setCustomKeys {
                    key("appName", "wearApp")
                }
            }
        }

        val androidContext = this@ConfettiApplication
        initWearApp(androidContext) {
            workManagerFactory()
        }

        val callFactory = get<Call.Factory>()
        URL.setURLStreamHandlerFactory(FirebaseUrlFactory { request ->
            val requestType = if (request.url.pathSegments.contains("firelog")) {
                RequestType.LogsRequest
            } else {
                RequestType.ApiRequest
            }

            val finalRequest = request.withDefaultRequestType(requestType)

            callFactory.newCall(finalRequest)
        })

        setupDailyRefresh(get())
    }

    companion object {
        fun initWearApp(
            androidContext: Context,
            extraModules: List<Module> = listOf(),
            extraDeclaration: KoinAppDeclaration = {}
        ) {
            initKoin {
                androidLogger()
                androidContext(androidContext)
                modules(appModule, *extraModules.toTypedArray())
                extraDeclaration()
            }
        }
    }
}
