package dev.johnoreilly.confetti.wear

import android.app.Application
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.wear.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@TestApplication)
            modules(appModule, instrumentedTestModule)
        }
    }
}

private val instrumentedTestModule = module {
//    factory<Something> { FakeSomething() }
}