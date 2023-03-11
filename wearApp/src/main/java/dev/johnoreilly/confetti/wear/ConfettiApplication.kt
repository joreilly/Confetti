package dev.johnoreilly.confetti.wear

import android.app.Application
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.workDataOf
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.wear.di.appModule
import dev.johnoreilly.confetti.work.RefreshWorker
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import java.time.Duration

class ConfettiApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader = get()

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            Firebase.crashlytics.setCustomKeys {
                key("appName", "wearApp")
            }
        }

        initKoin {
            androidLogger()
            androidContext(this@ConfettiApplication)
            modules(appModule)

            workManagerFactory()
        }

        setupDailyRefresh()
    }

    private fun setupDailyRefresh() {
        val data = workDataOf(
            RefreshWorker.FetchConferencesKey to true,
            RefreshWorker.FetchImagesKey to true
        )

        PeriodicWorkRequestBuilder<RefreshWorker>(Duration.ofDays(1))
            .setInputData(data)
            .setConstraints(
                Constraints(
                    requiresCharging = true,
                    requiredNetworkType = NetworkType.CONNECTED
                )
            )
            .build()
    }
}