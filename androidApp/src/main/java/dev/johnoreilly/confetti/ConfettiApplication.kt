package dev.johnoreilly.confetti

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.di.appModule
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.work.SessionNotificationWorker
import dev.johnoreilly.confetti.work.setupDailyRefresh
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory

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
                    key("appName", "androidApp")
                }
            } else {
                Firebase.crashlytics.setCrashlyticsCollectionEnabled(false)
            }
        }

        // Initialize Logging.
        Napier.base(DebugAntilog())

        initKoin {
            androidLogger()
            androidContext(this@ConfettiApplication)
            modules(appModule)

            workManagerFactory()
        }

        val workManager = get<WorkManager>()
        setupDailyRefresh(workManager)

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            get<AppSettings>().experimentalFeaturesEnabledFlow.collect { isEnabled ->
                if (isEnabled) {
                    SessionNotificationWorker.startPeriodicWorkRequest(workManager)
                } else {
                    SessionNotificationWorker.cancelWorkRequest(workManager)
                }
            }
        }
    }
}
