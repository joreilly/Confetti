package dev.johnoreilly.confetti

import android.app.Application
import dev.johnoreilly.confetti.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ConfettiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ConfettiApplication)
            modules(appModule)
        }
    }
}