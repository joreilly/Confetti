package dev.johnoreilly.confetti

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import dev.johnoreilly.confetti.di.appModule
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.work.SessionNotificationSender
import dev.johnoreilly.confetti.work.setupDailyRefresh
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext

class ConfettiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val isFirebaseInstalled =
            FirebaseApp.getApps(this).isNotEmpty() || FirebaseApp.initializeApp(this) != null

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

        if (GlobalContext.getOrNull() == null) {
            initKoin {
                androidLogger()
                androidContext(this@ConfettiApplication)
                modules(appModule)

                workManagerFactory()
            }
        }

        val workManager = get<WorkManager>()
        setupDailyRefresh(workManager)

        ProcessLifecycleOwner.get().lifecycleScope.launch {
            get<SessionNotificationSender>().updateSchedule()
        }
    }
}
