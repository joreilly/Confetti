@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.di

import com.russhwolf.settings.ExperimentalSettingsApi
import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect fun platformModule(): Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        modules(commonModule())
        appDeclaration()
    }

// called by iOS client
fun initKoin() = initKoin() {}

fun commonModule() = module {
    includes(platformModule())

    singleOf(::ConfettiRepository)
    singleOf(::AppSettings)
    singleOf(::ApolloClientCache)
}

expect fun getDatabaseName(conference: String, uid: String?): String
