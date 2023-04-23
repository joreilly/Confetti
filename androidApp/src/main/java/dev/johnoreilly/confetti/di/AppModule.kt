@file:OptIn(ExperimentalHorologistApi::class)
@file:Suppress("RemoveExplicitTypeArguments")

package dev.johnoreilly.confetti.di

import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import dev.johnoreilly.confetti.*
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.DefaultAuthentication
import dev.johnoreilly.confetti.settings.SettingsViewModel
import dev.johnoreilly.confetti.wear.WearSettingsSync
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::AppViewModel)
    viewModelOf(::ConferencesViewModel)
    viewModelOf(::SessionDetailsViewModel)
    viewModelOf(::SpeakerDetailsViewModel)
    viewModelOf(::SettingsViewModel)
    viewModel { params -> SpeakersViewModel(params.get()) }
    viewModel { params ->
        BookmarksViewModel(
            get(),
            get(parameters = { params })
        )
    }

    viewModel { params ->
        val vmParams = params.get<SessionsViewModelParams>()
        SessionsViewModel(vmParams.conference, vmParams.uid, vmParams.tokenProvider)
    }
    viewModel { params ->
        SearchViewModel(
            get(parameters = { params }),
            get(parameters = {
                parametersOf(params.get<SessionsViewModelParams>().conference)
            })
        )
    }

    single {
        ConfettiRepository().apply {
            addConferenceListener {
                get<WearSettingsSync>().setConference(it)
            }
        }
    }

    single<ConferenceRefresh> { WorkManagerConferenceRefresh(get()) }

    singleOf(::WorkManagerConferenceRefresh) { bind<ConferenceRefresh>() }
    singleOf(::DefaultAuthentication) { bind<Authentication>() }

    single<PhoneDataLayerAppHelper> {
        PhoneDataLayerAppHelper(androidContext(), get())
    }

    single<WearSettingsSync> {
        WearSettingsSync(get(), get(), androidContext(), get())
    }
}
