@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.auth.data.googlesignin.GoogleSignInEventListener
import com.google.android.horologist.auth.ui.common.screens.prompt.SignInPromptViewModel
import com.google.android.horologist.auth.ui.googlesignin.signin.GoogleSignInViewModel
import dev.johnoreilly.confetti.ConferenceRefresh
import dev.johnoreilly.confetti.wear.WearAppViewModel
import dev.johnoreilly.confetti.wear.auth.ConfettiGoogleSignOutViewModel
import dev.johnoreilly.confetti.wear.conferences.ConferencesViewModel
import dev.johnoreilly.confetti.wear.data.auth.GoogleSignInAuthUserRepository
import dev.johnoreilly.confetti.wear.data.auth.MobileAuthRepository
import dev.johnoreilly.confetti.wear.home.HomeViewModel
import dev.johnoreilly.confetti.wear.sessiondetails.SessionDetailsViewModel
import dev.johnoreilly.confetti.wear.sessions.SessionsViewModel
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import dev.johnoreilly.confetti.wear.settings.SettingsViewModel
import dev.johnoreilly.confetti.wear.speakerdetails.SpeakerDetailsViewModel
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@OptIn(ExperimentalHorologistApi::class, ExperimentalHorologistApi::class)
val appModule = module {
    viewModelOf(::SessionDetailsViewModel)
    viewModelOf(::SpeakerDetailsViewModel)
    viewModelOf(::ConferencesViewModel)
    viewModelOf(::SessionsViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::SignInPromptViewModel)
    viewModelOf(::ConfettiGoogleSignOutViewModel)
    viewModelOf(::GoogleSignInViewModel)
    viewModelOf(::WearAppViewModel)
    singleOf(::PhoneSettingsSync)
    single { GoogleSignIn.getClient(get<Context>(), GoogleSignInOptions.DEFAULT_SIGN_IN) }
    single { MobileAuthRepository(get(), get(), get()) }
    singleOf(::GoogleSignInAuthUserRepository) { bind<GoogleSignInEventListener>() }
    singleOf(::WorkManagerConferenceRefresh) { bind<ConferenceRefresh>() }
}
