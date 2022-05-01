package dev.johnoreilly.confetti

import android.app.Application
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class ConfettiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            // workaround for https://github.com/InsertKoinIO/koin/issues/1188
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@ConfettiApplication)
            modules(appModule)
        }
    }

}