package dev.johnoreilly.confetti.wear.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.wear.tiles.TileService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.horologist.auth.ui.common.screens.prompt.SignInPromptViewModel
import com.google.android.horologist.auth.ui.googlesignin.signin.GoogleSignInViewModel
import com.google.android.horologist.networks.data.DataRequestRepository
import com.google.android.horologist.networks.data.InMemoryDataRequestRepository
import com.google.android.horologist.networks.data.RequestType
import com.google.android.horologist.networks.highbandwidth.HighBandwidthNetworkMediator
import com.google.android.horologist.networks.highbandwidth.StandardHighBandwidthNetworkMediator
import com.google.android.horologist.networks.logging.NetworkStatusLogger
import com.google.android.horologist.networks.okhttp.NetworkAwareCallFactory
import com.google.android.horologist.networks.okhttp.NetworkSelectingCallFactory
import com.google.android.horologist.networks.request.NetworkRequester
import com.google.android.horologist.networks.request.NetworkRequesterImpl
import com.google.android.horologist.networks.rules.NetworkingRules
import com.google.android.horologist.networks.rules.NetworkingRulesEngine
import com.google.android.horologist.networks.status.NetworkRepository
import com.google.android.horologist.networks.status.NetworkRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.DefaultAuthentication
import dev.johnoreilly.confetti.decompose.ConferenceRefresh
import dev.johnoreilly.confetti.wear.complication.ComplicationUpdater
import dev.johnoreilly.confetti.wear.data.auth.FirebaseAuthUserRepository
import dev.johnoreilly.confetti.wear.data.auth.FirebaseAuthUserRepositoryImpl
import dev.johnoreilly.confetti.wear.networks.WearNetworkingRules
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import dev.johnoreilly.confetti.wear.tile.TileUpdater
import dev.johnoreilly.confetti.wear.work.WearConferenceSetting
import dev.johnoreilly.confetti.work.ConferenceSetting
import dev.johnoreilly.confetti.work.RefreshWorker
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import okhttp3.Call
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

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

    single<NetworkingRules> { WearNetworkingRules }

    single<NetworkStatusLogger> {
        NetworkStatusLogger.Logging
    }

    single<NetworkRequester> {
        NetworkRequesterImpl(
            connectivityManager = androidContext().getSystemService(ConnectivityManager::class.java)
        )
    }

    single<HighBandwidthNetworkMediator> {
        StandardHighBandwidthNetworkMediator(
            logger = get(),
            networkRequester = get(),
            coroutineScope = get(),
            delayToRelease = 3.seconds,
        )
    }

    single<DataRequestRepository> {
        InMemoryDataRequestRepository()
    }

    single<NetworkRepository> {
        NetworkRepositoryImpl.fromContext(
            application = androidContext(),
            coroutineScope = get(),
        )
    }

    single<NetworkingRulesEngine> {
        NetworkingRulesEngine(
            networkRepository = get(),
            logger = get(),
            networkingRules = get(),
        )
    }

    single<Call.Factory> {
        NetworkSelectingCallFactory(
            networkingRulesEngine = get(),
            highBandwidthNetworkMediator = get(),
            networkRepository = get(),
            dataRequestRepository = get(),
            rootClient = get(),
            coroutineScope = get(),
            timeout = 3.seconds,
            logger = get(),
        )
    }

    single<Call.Factory>(qualifier = named("API")) {
        NetworkAwareCallFactory(
            delegate = get<Call.Factory>(),
            defaultRequestType = RequestType.ApiRequest,
        )
    }

    single<Call.Factory>(qualifier = named("images")) {
        NetworkAwareCallFactory(
            delegate = get<Call.Factory>(),
            defaultRequestType = RequestType.ImageRequest,
        )
    }

    single<Call.Factory>(qualifier = named("logs")) {
        NetworkAwareCallFactory(
            delegate = get<Call.Factory>(),
            defaultRequestType = RequestType.LogsRequest,
        )
    }

    single<FirebaseAuthUserRepository> { FirebaseAuthUserRepositoryImpl(get(), get()) }
    singleOf(::WorkManagerConferenceRefresh) { bind<ConferenceRefresh>() }
    singleOf(::WearConferenceSetting) { bind<ConferenceSetting>() }
    workerOf(::RefreshWorker)
}
