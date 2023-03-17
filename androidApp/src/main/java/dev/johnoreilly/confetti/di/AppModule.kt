package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.AppViewModel
import dev.johnoreilly.confetti.ConferenceRefresh
import dev.johnoreilly.confetti.ConferencesViewModel
import dev.johnoreilly.confetti.SessionsViewModel
import dev.johnoreilly.confetti.SpeakersViewModel
import dev.johnoreilly.confetti.TokenProvider
import dev.johnoreilly.confetti.account.Authentication
import dev.johnoreilly.confetti.sessiondetails.SessionDetailsViewModel
import dev.johnoreilly.confetti.speakerdetails.SpeakerDetailsViewModel
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { SessionsViewModel() }
    viewModel { AppViewModel() }
    viewModel { ConferencesViewModel() }
    viewModel { SpeakersViewModel() }
    viewModel { SessionDetailsViewModel(get(), get()) }
    viewModel { SpeakerDetailsViewModel(get(), get()) }
    single<ConferenceRefresh> { WorkManagerConferenceRefresh(get()) }

    single {
        Authentication()
    }
    single<TokenProvider> {
        object : TokenProvider {
            override suspend fun token(forceRefresh: Boolean): String? {
                return get<Authentication>().idToken(forceRefresh)
            }
        }
    }
}
