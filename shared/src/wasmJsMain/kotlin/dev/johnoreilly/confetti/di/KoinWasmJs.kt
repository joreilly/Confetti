@file:OptIn(ExperimentalSettingsApi::class, ApolloExperimental::class)

package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.serialization.toRuntimeObservable
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.WasmDateService
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
actual fun platformModule() = module {
    single<Authentication> { Authentication.Disabled }
    single<FlowSettings> {  StorageSettings().toRuntimeObservable().toFlowSettings() }
    singleOf(::WasmDateService) { bind<DateService>() }
    single<FetchPolicy> {
        FetchPolicy.CacheAndNetwork
    }
}

actual fun getNormalizedCacheFactory(conference: String, uid: String?): NormalizedCacheFactory {
    return MemoryCacheFactory(10 * 1024 * 1024)
}