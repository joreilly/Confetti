@file:OptIn(ExperimentalHorologistDataLayerApi::class)
@file:Suppress("RemoveExplicitTypeArguments")

package dev.johnoreilly.confetti.di

import com.google.android.horologist.data.ExperimentalHorologistDataLayerApi
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import dev.johnoreilly.confetti.AppViewModel
import dev.johnoreilly.confetti.ConferenceRefresh
import dev.johnoreilly.confetti.ConferencesViewModel
import dev.johnoreilly.confetti.SessionsViewModel
import dev.johnoreilly.confetti.SpeakersViewModel
import dev.johnoreilly.confetti.TokenProvider
import dev.johnoreilly.confetti.account.AccountViewModel
import dev.johnoreilly.confetti.account.Authentication
import dev.johnoreilly.confetti.sessiondetails.SessionDetailsViewModel
import dev.johnoreilly.confetti.speakerdetails.SpeakerDetailsViewModel
import dev.johnoreilly.confetti.wear.WearSettingsSync
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { SessionsViewModel() }
    viewModel { AppViewModel() }
    viewModel { ConferencesViewModel() }
    viewModel { SpeakersViewModel() }
    viewModel { SessionDetailsViewModel(get(), get()) }
    viewModel { SpeakerDetailsViewModel(get(), get()) }
    viewModel { AccountViewModel(get(), get(), get()) }
    single<ConferenceRefresh> { WorkManagerConferenceRefresh(get()) }

    single<Authentication> {
        Authentication()
    }
    single<TokenProvider> {
        object : TokenProvider {
            override suspend fun token(forceRefresh: Boolean): String? {
                return get<Authentication>().idToken(forceRefresh)
            }
        }
    }

    single<PhoneDataLayerAppHelper> {
        PhoneDataLayerAppHelper(androidContext(), get())
    }

    single<WearSettingsSync> {
        WearSettingsSync(get(), get(), androidContext(), get())
    }
}
