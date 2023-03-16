package dev.johnoreilly.confetti

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.di.appModule
import dev.johnoreilly.confetti.work.RefreshWorker
import dev.johnoreilly.confetti.work.setupDailyRefresh
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory

class ConfettiApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader = get()

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
            Firebase.crashlytics.setCustomKeys {
                key("appName", "androidApp")
            }
        } else {
            Firebase.crashlytics.setCrashlyticsCollectionEnabled(false)
        }
        
        initKoin {
            androidLogger()
            androidContext(this@ConfettiApplication)
            modules(appModule)

            workManagerFactory()
        }

        setupDailyRefresh(get())
    }

}