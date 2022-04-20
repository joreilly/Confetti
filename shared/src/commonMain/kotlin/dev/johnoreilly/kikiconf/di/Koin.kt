package dev.johnoreilly.kikiconf.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import dev.johnoreilly.kikiconf.AppSettings
import dev.johnoreilly.kikiconf.KikiConfRepository
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
    single { KikiConfRepository() }
    single { createApolloClient(get()) }
    single { AppSettings(get()) }
}

fun createApolloClient(sqlNormalizedCacheFactory: NormalizedCacheFactory): ApolloClient {
    val memoryFirstThenSqlCacheFactory = MemoryCacheFactory(10 * 1024 * 1024)
        .chain(sqlNormalizedCacheFactory)

    return ApolloClient.Builder()
        .serverUrl("https://kiki-conf.ew.r.appspot.com/graphql")
        .normalizedCache(memoryFirstThenSqlCacheFactory, writeToCacheAsynchronously = true)
        .build()
}
