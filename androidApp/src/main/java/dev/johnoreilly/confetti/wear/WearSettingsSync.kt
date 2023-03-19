@file:OptIn(ExperimentalHorologistDataLayerApi::class)

package dev.johnoreilly.confetti.wear

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.horologist.data.ExperimentalHorologistDataLayerApi
import com.google.android.horologist.data.ProtoDataStoreHelper.protoDataStore
import com.google.android.horologist.data.WearDataLayerRegistry
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import dev.johnoreilly.confetti.ui.colorScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class WearSettingsSync(
    val dataLayerRegistry: WearDataLayerRegistry,
    val phoneDataLayerRegistry: PhoneDataLayerAppHelper,
    val context: Context,
    val coroutineScope: CoroutineScope
) {
    val settingsDataStore = dataLayerRegistry.protoDataStore<WearSettings>(coroutineScope)

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

    val wearNodes = flow {
        emit(phoneDataLayerRegistry.connectedNodes())
    }

    suspend fun updateWearTheme() {
        if (isAvailable()) {
            val connectedNodes = phoneDataLayerRegistry.connectedAndInstalledNodes.first()

            if (connectedNodes.isNotEmpty()) {
                val theme = colorScheme(false, false, false, context)

                settingsDataStore.updateData {
                    it.copy(
                        theme = Theme(
                            primary = toLong(theme.primary),
                            primaryVariant = toLong(theme.primaryContainer),
                            secondary = toLong(theme.secondary),
                            secondaryVariant = toLong(theme.secondaryContainer),
                            surface = toLong(theme.surface),
                            error = toLong(theme.error),
                            onPrimary = toLong(theme.onPrimary),
                            onSecondary = toLong(theme.onSecondary),
                            onBackground = toLong(theme.onBackground),
                            onSurface = toLong(theme.onSurface),
                            onSurfaceVariant = toLong(theme.onSurfaceVariant),
                            onError = toLong(theme.onError),
                        )
                    )
                }
            }
        }
    }

    private fun toLong(primary: Color) = primary.value.toLong()

    suspend fun installOnWearNode(nodeId: String) {
        try {
            phoneDataLayerRegistry.installOnNode(nodeId)
        } catch (rie: Exception) {
            // likely RemoteIntentException due to emulator without playstore
            // TODO handle error
        }
    }
}