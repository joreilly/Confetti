@file:OptIn(ExperimentalHorologistAuthDataApi::class, ExperimentalHorologistAuthUiApi::class,
    ExperimentalHorologistDataLayerApi::class
)

package dev.johnoreilly.confetti.wear.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.horologist.auth.data.ExperimentalHorologistAuthDataApi
import com.google.android.horologist.auth.ui.ExperimentalHorologistAuthUiApi
import com.google.android.horologist.auth.ui.common.screens.prompt.SignInPromptViewModel
import com.google.android.horologist.auth.ui.googlesignin.signin.GoogleSignInViewModel
import com.google.android.horologist.data.ExperimentalHorologistDataLayerApi
import dev.johnoreilly.confetti.ConferenceRefresh
import dev.johnoreilly.confetti.wear.WearAppViewModel
import dev.johnoreilly.confetti.wear.auth.ConfettiGoogleSignOutViewModel
import dev.johnoreilly.confetti.wear.conferences.ConferencesViewModel
import dev.johnoreilly.confetti.wear.data.auth.GoogleSignInAuthUserRepository
import dev.johnoreilly.confetti.wear.home.HomeViewModel
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsViewModel
import dev.johnoreilly.confetti.wear.sessions.SessionsViewModel
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import dev.johnoreilly.confetti.wear.settings.SettingsViewModel
import dev.johnoreilly.confetti.wear.speakerdetails.SpeakerDetailsViewModel
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@OptIn(ExperimentalHorologistAuthDataApi::class, ExperimentalHorologistAuthUiApi::class)
val appModule = module {
    viewModel { SessionDetailsViewModel(get(), get(), get()) }
    viewModel { SpeakerDetailsViewModel(get(), get()) }
    viewModel { ConferencesViewModel(get(), get(), get()) }
    viewModel { SessionsViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { GoogleSignInViewModel(get(), get<GoogleSignInAuthUserRepository>()) }
    viewModel { SignInPromptViewModel(get()) }
    viewModel { ConfettiGoogleSignOutViewModel(get()) }
    viewModel { WearAppViewModel() }
    single { PhoneSettingsSync(get()) }
    single { GoogleSignIn.getClient(get<Context>(), GoogleSignInOptions.DEFAULT_SIGN_IN) }
    single { GoogleSignInAuthUserRepository(get(), get()) }
    single<ConferenceRefresh> { WorkManagerConferenceRefresh(get()) }
}
