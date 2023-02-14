package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.ConfettiViewModel
import dev.johnoreilly.confetti.sessiondetails.SessionDetailsViewModel
import dev.johnoreilly.confetti.speakerdetails.SpeakerDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { ConfettiViewModel() }
    viewModel { SessionDetailsViewModel(get(), get()) }
    viewModel { SpeakerDetailsViewModel(get(), get()) }
    single { FetchPolicy.CacheAndNetwork }
}
