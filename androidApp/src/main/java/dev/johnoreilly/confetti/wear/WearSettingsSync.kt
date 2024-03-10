@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear

import android.os.Build
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.data.ProtoDataStoreHelper.protoDataStore
import com.google.android.horologist.data.WearDataLayerRegistry
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import dev.johnoreilly.confetti.wear.proto.WearSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearSettingsSync(
    val dataLayerRegistry: WearDataLayerRegistry,
    val phoneDataLayerRegistry: PhoneDataLayerAppHelper,
    val coroutineScope: CoroutineScope
) {
    private var job: Job? = null

    val settingsDataStore by lazy { dataLayerRegistry.protoDataStore<WearSettings>(coroutineScope) }

    suspend fun isAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < 23)
            return false

        return try {
            GoogleApiAvailability.getInstance()
                .checkApiAvailability(dataLayerRegistry.dataClient)
                .await()

            true
        } catch (e: AvailabilityException) {
            Log.d(
                "WearSettingsSync",
                "DataClient API is not available in this device. WearSettingsSync will fail silently and all functionality will be no-op."
            )
            false
        }
    }

    val settingsFlow: Flow<WearSettings> = flow {
        if (isAvailable()) {
            emitAll(settingsDataStore.data)
        } else {
            emit(WearSettings())
        }
    }.catch {
        emit(WearSettings())
    }

    val wearNodes = flow {
        emit(phoneDataLayerRegistry.connectedNodes())
    }.catch { emit(listOf()) }

    // public non-suspend function to be called from button callbacks, etc...
    fun installOnWearNode(nodeId: String) {
        if (job != null) {
            // already installing, skip
            return
        }

        // coroutineScope at this point is a app-global scope
        job = coroutineScope.launch {
            installOnWearNodeInternal(nodeId)
            job = null
        }
    }

    private suspend fun installOnWearNodeInternal(nodeId: String) {
        try {
            phoneDataLayerRegistry.installOnNode(nodeId)
        } catch (rie: Exception) {
            // likely RemoteIntentException due to emulator without playstore
            // TODO handle error
        }
    }

    suspend fun setConference(conference: String, colorScheme: String?) {
        if (isAvailable()) {
            settingsDataStore.updateData {
                it.copy(conference = conference, color_scheme = colorScheme)
            }
        }
    }
}