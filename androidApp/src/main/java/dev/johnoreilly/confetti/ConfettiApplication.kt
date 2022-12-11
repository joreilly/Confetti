package dev.johnoreilly.confetti

import android.app.Application
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class ConfettiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@ConfettiApplication)
            modules(appModule)
        }
    }

}