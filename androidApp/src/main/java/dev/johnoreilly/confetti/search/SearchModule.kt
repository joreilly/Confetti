package dev.johnoreilly.confetti.search

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

fun searchModule() = module {
    viewModelOf(::SearchViewModel)
}
