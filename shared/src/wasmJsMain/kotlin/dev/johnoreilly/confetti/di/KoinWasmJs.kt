@file:OptIn(ExperimentalSettingsApi::class, ApolloExperimental::class)

package dev.johnoreilly.confetti.di

import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.observable.makeObservable
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.dev.johnoreilly.confetti.work.SessionNotificationSender
import dev.johnoreilly.confetti.utils.DateService
import dev.johnoreilly.confetti.utils.WasmDateService
import dev.johnoreilly.confetti.work.NotificationSender
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
actual fun platformModule() = module {
    single<Authentication> { Authentication.Disabled }
    single<FlowSettings> {  StorageSettings().makeObservable().toFlowSettings() }
    singleOf(::WasmDateService) { bind<DateService>() }
    single<FetchPolicy> {
        FetchPolicy.CacheAndNetwork
    }
    single<NotificationSender> { SessionNotificationSender() }
}

actual fun getNormalizedCacheFactory(conference: String, uid: String?): NormalizedCacheFactory {
    return MemoryCacheFactory(10 * 1024 * 1024)
}