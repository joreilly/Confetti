package dev.johnoreilly.confetti.wear.di

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.wear.conferences.ConferencesViewModel
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsViewModel
import dev.johnoreilly.confetti.wear.sessions.SessionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { ConfettiViewModel() }
    viewModel { SessionDetailsViewModel(get(), get(), get()) }
    viewModel { ConferencesViewModel(get()) }
    viewModel { SessionsViewModel(get(), get()) }
    single {
        // Assume an offline first strategy for Wear
        // Eventually use the mobile to drive updates
        FetchPolicy.CacheFirst
    }
}
