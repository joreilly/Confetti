@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.data.ProtoDataStoreHelper.protoDataStore
import com.google.android.horologist.data.WearDataLayerRegistry
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import dev.johnoreilly.confetti.BuildConfig
import dev.johnoreilly.confetti.ui.colorScheme
import dev.johnoreilly.confetti.wear.proto.Theme
import dev.johnoreilly.confetti.wear.proto.WearSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okio.Buffer

class WearSettingsSync(
    val dataLayerRegistry: WearDataLayerRegistry,
    val phoneDataLayerRegistry: PhoneDataLayerAppHelper,
    val context: Context,
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

    suspend fun clearWearTheme() {
        if (isAvailable()) {
            settingsDataStore.updateData {
                it.copy(theme = null)
            }
        }
    }

    suspend fun updateWearTheme(theme: ColorScheme) {
        if (isAvailable()) {
            if (BuildConfig.DEBUG) {
                println("primary = ${theme.primary.hex()}L.toInt(),")
                println("primaryContainer = ${theme.primaryContainer.hex()}L.toInt(),")
                println("secondary = ${theme.secondary.hex()}L.toInt(),")
                println("secondaryContainer = ${theme.secondaryContainer.hex()}L.toInt(),")
                println("surface = ${theme.surface.hex()}L.toInt(),")
                println("error = ${theme.error.hex()}L.toInt(),")
                println("onPrimary = ${theme.onPrimary.hex()}L.toInt(),")
                println("onSecondary = ${theme.onSecondary.hex()}L.toInt(),")
                println("onBackground = ${theme.onBackground.hex()}L.toInt(),")
                println("onSurface = ${theme.onSurface.hex()}L.toInt(),")
                println("onSurfaceVariant = ${theme.onSurfaceVariant.hex()}L.toInt(),")
                println("onError = ${theme.onError.hex()}L.toInt(),")
            }

            settingsDataStore.updateData {
                val protoTheme = Theme(
                    primary = theme.primary.toArgb(),
                    primaryVariant = theme.primaryContainer.toArgb(),
                    secondary = theme.secondary.toArgb(),
                    secondaryVariant = theme.secondaryContainer.toArgb(),
                    surface = theme.surface.toArgb(),
                    error = theme.error.toArgb(),
                    onPrimary = theme.onPrimary.toArgb(),
                    onSecondary = theme.onSecondary.toArgb(),
                    onBackground = theme.onBackground.toArgb(),
                    onSurface = theme.onSurface.toArgb(),
                    onSurfaceVariant = theme.onSurfaceVariant.toArgb(),
                    onError = theme.onError.toArgb(),
                )
                it.copy(
                    theme = protoTheme
                )
            }
        }
    }

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

    suspend fun setConference(conference: String) {
        if (isAvailable()) {
            settingsDataStore.updateData {
                it.copy(conference = conference)
            }
        }
    }

    fun Color.hex(): String {
        return "0x" + Buffer().writeInt(this.toArgb()).readByteString().hex()
    }
}