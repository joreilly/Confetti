@file:Suppress("RemoveExplicitTypeArguments")
@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.di

import androidx.credentials.CredentialManager
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.DefaultAuthentication
import dev.johnoreilly.confetti.decompose.ConferenceRefresh
import dev.johnoreilly.confetti.decompose.SettingsComponent
import dev.johnoreilly.confetti.settings.DefaultSettingsComponent
import dev.johnoreilly.confetti.wear.WearSettingsSync
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    single<SettingsComponent> {
        val lifecycle = LifecycleRegistry()
        DefaultSettingsComponent(
            componentContext = DefaultComponentContext(lifecycle),
            appSettings = get(),
            authentication = get(),
        ).also { lifecycle.resume() }
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
    singleOf(::DefaultAuthentication) { bind<Authentication>() }

    single<PhoneDataLayerAppHelper> {
        PhoneDataLayerAppHelper(androidContext(), get())
    }

    single<WearSettingsSync> {
        WearSettingsSync(get(), get(), get())
    }
}
