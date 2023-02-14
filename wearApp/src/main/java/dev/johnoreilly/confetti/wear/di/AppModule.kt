package dev.johnoreilly.confetti.wear.di

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { ConfettiViewModel() }
    viewModel { SessionDetailsViewModel(get(), get(), get()) }
    single { FetchPolicy.CacheFirst }
}
