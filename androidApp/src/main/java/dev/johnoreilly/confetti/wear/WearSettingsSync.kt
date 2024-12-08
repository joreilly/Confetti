@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear

import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.data.ProtoDataStoreHelper.protoDataStore
import com.google.android.horologist.data.WearDataLayerRegistry
import com.google.android.horologist.datalayer.phone.PhoneDataLayerAppHelper
import dev.johnoreilly.confetti.wear.proto.WearSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class WearSettingsSync(
    val dataLayerRegistry: WearDataLayerRegistry,
    val phoneDataLayerRegistry: PhoneDataLayerAppHelper,
    val coroutineScope: CoroutineScope
) {
    val settingsDataStore by lazy { dataLayerRegistry.protoDataStore<WearSettings>(coroutineScope) }

    suspend fun setConference(conference: String, colorScheme: String?) {
        if (phoneDataLayerRegistry.isAvailable()) {
            settingsDataStore.updateData {
                it.copy(conference = conference, color_scheme = colorScheme)
            }
        }
    }

    fun updateIdToken(idToken: String?) {
        coroutineScope.launch {
            if (phoneDataLayerRegistry.isAvailable()) {
                settingsDataStore.updateData {
                    it.copy(idToken = idToken)
                }
            }
        }
    }
}