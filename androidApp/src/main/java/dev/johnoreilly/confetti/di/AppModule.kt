package dev.johnoreilly.confetti.di

import dev.johnoreilly.confetti.ConfettiViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { ConfettiViewModel(get()) }
}
