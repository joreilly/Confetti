package dev.johnoreilly.kikiconf.di

import dev.johnoreilly.kikiconf.KikiConfRepository
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule())
    }

// called by iOS etc
fun initKoin() = initKoin() {}

fun commonModule() = module {
    single { KikiConfRepository() }
}
