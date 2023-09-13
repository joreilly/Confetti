package dev.johnoreilly.confetti.wear.di

import android.content.Context
import androidx.wear.tiles.TileService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.horologist.auth.ui.common.screens.prompt.SignInPromptViewModel
import com.google.android.horologist.auth.ui.googlesignin.signin.GoogleSignInViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.DefaultAuthentication
import dev.johnoreilly.confetti.decompose.ConferenceRefresh
import dev.johnoreilly.confetti.wear.complication.ComplicationUpdater
import dev.johnoreilly.confetti.wear.data.auth.FirebaseAuthUserRepository
import dev.johnoreilly.confetti.wear.data.auth.FirebaseAuthUserRepositoryImpl
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import dev.johnoreilly.confetti.wear.tile.TileUpdater
import dev.johnoreilly.confetti.wear.work.WearConferenceSetting
import dev.johnoreilly.confetti.work.ConferenceSetting
import dev.johnoreilly.confetti.work.RefreshWorker
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::SignInPromptViewModel)
    viewModelOf(::GoogleSignInViewModel)
    singleOf(::PhoneSettingsSync)
    single { TileService.getUpdater(androidContext()) }
    singleOf(::ComplicationUpdater)
    singleOf(::TileUpdater)
    single {
        try {
            DefaultAuthentication(get())
        } catch (ise: IllegalStateException) {
            // We wont have firebase when running in Robolectric
            // TODO override just in robolectric
            Authentication.Disabled
        }
    }
    single {
        GoogleSignIn.getClient(
            get<Context>(), GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(androidContext().getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }
    single {
        Firebase.auth
    }
    single<FirebaseAuthUserRepository> { FirebaseAuthUserRepositoryImpl(get(), get()) }
    singleOf(::WorkManagerConferenceRefresh) { bind<ConferenceRefresh>() }
    singleOf(::WearConferenceSetting) { bind<ConferenceSetting>() }
    workerOf(::RefreshWorker)
}
