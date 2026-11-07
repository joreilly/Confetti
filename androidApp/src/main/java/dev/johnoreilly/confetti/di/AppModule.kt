@file:Suppress("RemoveExplicitTypeArguments") @file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.di

import androidx.credentials.CredentialManager
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.account.SignInProcess
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.DefaultAuthentication
import dev.johnoreilly.confetti.decompose.ConferenceRefresh
import dev.johnoreilly.confetti.wear.WearSettingsSync
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import dev.johnoreilly.confetti.ui.components.ConferenceComponentsFactory
import dev.johnoreilly.confetti.ui.components.NativeComponents
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    // What the app draws its conference components with: Confetti's own, unless the variant
    // includes a module that provides others (sduiDebug: :livery, per conference sdui arm).
    single<ConferenceComponents> {
        ConferenceComponentsFactory.load()?.create(
            androidContext(), okHttp = get(), analytics = get(),
            options = mapOf("forcedArm" to BuildConfig.LIVERY_FORCED_ARM),
        ) ?: NativeComponents()
    }
    single<ConfettiRepository> {
        ConfettiRepository().apply {
            addConferenceListener { conference, colorScheme ->
                get<WearSettingsSync>().setConference(conference, colorScheme)
            }
        }
    }

    single { CredentialManager.create(get()) }

    single<ConferenceRefresh> { WorkManagerConferenceRefresh(get()) }

    singleOf(::WorkManagerConferenceRefresh) { bind<ConferenceRefresh>() }
    single<Authentication> {
        val wearSettingsSync = get<WearSettingsSync>()
        DefaultAuthentication(
            coroutineScope = get(), onTokenChanged = { idToken ->
                wearSettingsSync.updateIdToken(idToken)
            }
        )
    }

    single<PhoneDataLayerAppHelper> {
        PhoneDataLayerAppHelper(androidContext(), get())
    }

    single<WearSettingsSync> {
        WearSettingsSync(get(), get(), get())
    }

    single<SignInProcess> {
        SignInProcess(
            credentialManager = get(),
            authentication = get(),
            webClientId = androidContext().getString(R.string.default_web_client_id)
        )
    }
}
