package dev.johnoreilly.confetti.wear

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.android.horologist.networks.okhttp.urlconnection.FirebaseUrlFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.Firebase
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.wear.di.appModule
import dev.johnoreilly.confetti.work.setupDailyRefresh
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import okhttp3.Call
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import java.net.URL

class ConfettiApplication : Application(), ImageLoaderFactory {

    private val isFirebaseInstalled
        get() = try {
            FirebaseApp.getInstance()
            true
        } catch (ise: IllegalStateException) {
            false
        }

    override fun newImageLoader(): ImageLoader = get()

    override fun onCreate() {
        super.onCreate()

        if (isFirebaseInstalled) {
            if (!BuildConfig.DEBUG) {
                Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
                Firebase.crashlytics.setCustomKeys {
                    key("appName", "wearApp")
                }
            }
        }

        // Initialize Logging.
        Napier.base(DebugAntilog())

        val androidContext = this@ConfettiApplication
        initWearApp(androidContext) {
            workManagerFactory()
        }

        URL.setURLStreamHandlerFactory(FirebaseUrlFactory(get<Call.Factory>(named("logs"))))

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
