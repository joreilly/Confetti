@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.tile

import android.content.Context
import androidx.concurrent.futures.await
import androidx.wear.tiles.TileService
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.data.WearDataLayerRegistry
import com.google.android.horologist.datalayer.watch.WearDataLayerAppHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.tasks.await

class TileSync(
    private val registry: WearDataLayerRegistry,
    private val wearAppHelper: WearDataLayerAppHelper,
) {
    private val executor = Dispatchers.Default.asExecutor()

    suspend fun trackInstalledTiles(context: Context) {
        registry.dataClient
            .putDataItem(PutDataRequest.create("/tile_tracking_enabled")).await()

        wearAppHelper.updateInstalledTiles()
    }
}