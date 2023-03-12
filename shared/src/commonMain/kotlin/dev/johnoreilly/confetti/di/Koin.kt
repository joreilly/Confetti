package dev.johnoreilly.confetti.di

import dev.johnoreilly.confetti.ApolloClientCache
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.TokenProvider
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect fun platformModule(): Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        modules(commonModule(), platformModule())
        appDeclaration()
    }

// called by iOS client
fun initKoin() = initKoin() {}

fun commonModule() = module {
    single { ConfettiRepository(get()) }
    single { AppSettings(get()) }
    single { ApolloClientCache() }
    single<TokenProvider> {
        object : TokenProvider {
            override suspend fun token(forceRefresh: Boolean): String? {
                return "signed-out"
            }
        }
    }
}

expect fun getDatabaseName(conference: String): String
