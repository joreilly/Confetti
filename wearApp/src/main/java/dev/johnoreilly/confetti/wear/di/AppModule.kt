package dev.johnoreilly.confetti.wear.di

import dev.johnoreilly.confetti.wear.ConfettiViewModel
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { ConfettiViewModel(get()) }
    viewModel { SessionDetailsViewModel(get(), get()) }
}
