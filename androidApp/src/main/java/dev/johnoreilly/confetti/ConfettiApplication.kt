package dev.johnoreilly.confetti

import android.app.Application
import android.os.Build
import androidx.appfunctions.service.AppFunctionConfiguration
import androidx.appsearch.platformstorage.PlatformStorage
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.appsearch.AppSearchManager
import dev.johnoreilly.confetti.di.appModule
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.work.SessionNotificationSender
import dev.johnoreilly.confetti.work.SessionNotificationWorker
import dev.johnoreilly.confetti.work.setupDailyRefresh
import kotlinx.coroutines.guava.asDeferred
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory

class ConfettiApplication : Application(), AppFunctionConfiguration.Provider {

    private val isFirebaseInstalled
        get() = try {
            FirebaseApp.getInstance()
            true
        } catch (ise: IllegalStateException) {
            false
        }

    override fun onCreate() {
        super.onCreate()

        if (isFirebaseInstalled) {
            if (!BuildConfig.DEBUG) {
                Firebase.crashlytics.isCrashlyticsCollectionEnabled = true
                Firebase.crashlytics.setCustomKeys {
                    key("appName", "androidApp")
                }
            } else {
                Firebase.crashlytics.isCrashlyticsCollectionEnabled = false
            }
        }

        initKoin {
            androidLogger()
            androidContext(this@ConfettiApplication)
            modules(appModule)

            workManagerFactory()
        }

        val workManager = get<WorkManager>()
        setupDailyRefresh(workManager)

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            get<SessionNotificationSender>().updateSchedule()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            get<AppSearchManager>().scheduleDailyUpdate()
        }
    }

    override val appFunctionConfiguration: AppFunctionConfiguration
        get() = get<AppFunctionConfiguration>()
}
