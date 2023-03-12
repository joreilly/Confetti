@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.di

import com.russhwolf.settings.ExperimentalSettingsApi
import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect fun platformModule(): Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule(), platformModule())
    }

// called by iOS client
fun initKoin() = initKoin() {}

fun commonModule() = module {
    single { ConfettiRepository(get()) }
    single { AppSettings(get()) }
    single { ApolloClientCache() }
}

expect fun getDatabaseName(conference: String): String
