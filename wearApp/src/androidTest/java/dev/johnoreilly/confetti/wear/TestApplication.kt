package dev.johnoreilly.confetti.wear

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.wear.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

class TestApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@TestApplication)
            modules(appModule, instrumentedTestModule(this@TestApplication))
        }
    }
}

fun instrumentedTestModule(context: Context): Module {
    return module {
        //    factory<Something> { FakeSomething() }   single<ObservableSettings>
    }
}