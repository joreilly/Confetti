package dev.johnoreilly.confetti.wear

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dev.johnoreilly.confetti.wear.di.appModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ConfettiApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader = get()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ConfettiApplication)
            modules(appModule)
        }
    }
}