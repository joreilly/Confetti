package dev.johnoreilly.mortycomposekmm.di

import dev.johnoreilly.kikiconf.android.KikiConfViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { KikiConfViewModel(get()) }
}
