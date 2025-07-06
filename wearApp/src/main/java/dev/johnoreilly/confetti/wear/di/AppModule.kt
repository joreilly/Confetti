@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.credentials.CredentialManager
import androidx.room.Room
import androidx.wear.tiles.TileService
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.apollographql.cache.normalized.FetchPolicy
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.datalayer.watch.WearDataLayerAppHelper
import com.google.android.horologist.networks.battery.BatteryStatusMonitor
import com.google.android.horologist.networks.data.DataRequestRepository
import com.google.android.horologist.networks.data.RequestType
import com.google.android.horologist.networks.db.DBDataRequestRepository
import com.google.android.horologist.networks.db.NetworkUsageDao
import com.google.android.horologist.networks.db.NetworkUsageDatabase
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.account.SignInProcess
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.DefaultAuthentication
import dev.johnoreilly.confetti.decompose.ConferenceRefresh
import dev.johnoreilly.confetti.wear.auth.WearAuthentication
import dev.johnoreilly.confetti.wear.complication.ComplicationUpdater
import dev.johnoreilly.confetti.wear.components.wearPhotoUrl
import dev.johnoreilly.confetti.wear.networks.WearNetworkingRules
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import dev.johnoreilly.confetti.wear.settings.WearPreferencesStore
import dev.johnoreilly.confetti.wear.tile.TileSync
import dev.johnoreilly.confetti.wear.tile.TileUpdater
import dev.johnoreilly.confetti.wear.work.WearConferenceSetting
import dev.johnoreilly.confetti.work.AvatarType
import dev.johnoreilly.confetti.work.ConferenceSetting
import dev.johnoreilly.confetti.work.RefreshWorker
import dev.johnoreilly.confetti.work.WorkManagerConferenceRefresh
import okhttp3.Call
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

val appModule = module {
    singleOf(::PhoneSettingsSync)
    single { TileService.getUpdater(androidContext()) }
    singleOf(::ComplicationUpdater)
    singleOf(::TileUpdater)
    single<Authentication> {
        val defaultAuthentication = try {
            DefaultAuthentication(get())
        } catch (ise: IllegalStateException) {
            // We wont have firebase when running in Robolectric
            // TODO override just in robolectric
            Authentication.Disabled
        }
        WearAuthentication(get(), defaultAuthentication, get())
    }
    single {
        Firebase.auth
    }
    single<FetchPolicy> {
        FetchPolicy.CacheFirst
    }

    single { CredentialManager.create(get()) }

    single<SignInProcess> {
        SignInProcess(
            credentialManager = get(),
            authentication = get(),
            webClientId = androidContext().getString(R.string.default_web_client_id),
            // Sign in with Google creation flow not supported on Wear 5.1
            useSignInWithGoogle = false,
        )
    }

    single<BatteryStatusMonitor> { BatteryStatusMonitor(androidContext()) }

    single<WearPreferencesStore> { WearPreferencesStore(androidContext(), get()) }

    single<NetworkingRules> {
        WearNetworkingRules(
            batteryStatusMonitor = get(),
            wearPreferences = get(),
            coroutineScope = get()
        )
    }

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

    single<NetworkUsageDatabase> {
        Room.databaseBuilder(
            androidContext(),
            NetworkUsageDatabase::class.java,
            "NetworkUsageDatabase",
        )
            .build()
    }

    single<NetworkUsageDao> {
        get<NetworkUsageDatabase>().networkUsageDao()
    }

    single<DataRequestRepository> {
        DBDataRequestRepository(get(), get())
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

    single<ImageLoader> {
        ImageLoader.Builder(androidContext())
            .callFactory { get(named("images")) }
            .crossfade(false)
            .respectCacheHeaders(false)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

    single<AvatarType> {
        { wearPhotoUrl }
    }

    single<TileSync> {
        TileSync(get(), get())
    }

    single<WearDataLayerAppHelper> {
        WearDataLayerAppHelper(context = androidContext(), registry = get(), scope = get())
    }

    singleOf(::WorkManagerConferenceRefresh) { bind<ConferenceRefresh>() }
    singleOf(::WearConferenceSetting) { bind<ConferenceSetting>() }
    workerOf(::RefreshWorker)
}
