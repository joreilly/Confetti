@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("RemoveExplicitTypeArguments")

package dev.johnoreilly.confetti.di

import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import dev.johnoreilly.confetti.AppViewModel
import dev.johnoreilly.confetti.ConferenceRefresh
import dev.johnoreilly.confetti.ConferencesViewModel
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.SearchViewModel
import dev.johnoreilly.confetti.SessionDetailsViewModel
import dev.johnoreilly.confetti.SessionsViewModel
import dev.johnoreilly.confetti.SpeakerDetailsViewModel
import dev.johnoreilly.confetti.SpeakersViewModel
import dev.johnoreilly.confetti.TokenProvider
import dev.johnoreilly.confetti.account.AccountViewModel
import dev.johnoreilly.confetti.account.Authentication
import dev.johnoreilly.confetti.settings.SettingsViewModel
import dev.johnoreilly.confetti.speakers.SpeakersAndroidViewModel
import dev.johnoreilly.confetti.wear.WearSettingsSync
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::SessionsViewModel)
    viewModelOf(::AppViewModel)
    viewModelOf(::ConferencesViewModel)
    viewModelOf(::SpeakersViewModel)
    viewModelOf(::SpeakersAndroidViewModel)
    viewModelOf(::SessionDetailsViewModel)
    viewModelOf(::SpeakerDetailsViewModel)
    viewModelOf(::AccountViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::SearchViewModel)

    single {
        ConfettiRepository().apply {
            addConferenceListener {
                get<WearSettingsSync>().setConference(it)
            }
        }
    }

    single<ConferenceRefresh> { WorkManagerConferenceRefresh(get()) }

    singleOf(::WorkManagerConferenceRefresh) { bind<ConferenceRefresh>() }
    singleOf(::Authentication)
    single { TokenProvider { forceRefresh -> get<Authentication>().idToken(forceRefresh) } }

    single<PhoneDataLayerAppHelper> {
        PhoneDataLayerAppHelper(androidContext(), get())
    }

    single<WearSettingsSync> {
        WearSettingsSync(get(), get(), androidContext(), get())
    }
}
