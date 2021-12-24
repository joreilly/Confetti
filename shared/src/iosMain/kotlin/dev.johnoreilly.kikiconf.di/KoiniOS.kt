package dev.johnoreilly.kikiconf.di

import com.russhwolf.settings.AppleSettings
import com.russhwolf.settings.ObservableSettings
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual fun platformModule() = module {
    single<ObservableSettings> { AppleSettings(NSUserDefaults.standardUserDefaults) }
}
