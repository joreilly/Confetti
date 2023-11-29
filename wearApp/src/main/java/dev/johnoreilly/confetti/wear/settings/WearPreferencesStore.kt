package dev.johnoreilly.confetti.wear.settings

import android.content.Context
import dev.johnoreilly.confetti.wear.proto.NetworkPreferences
import dev.johnoreilly.confetti.wear.proto.WearPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WearPreferencesStore(
    val context: Context,
    coroutineScope: CoroutineScope
) {
    val dataStore = context.wearPreferencesStore

    val networkPreferences: NetworkPreferences
        get() = preferences.value.networkPreferences ?: NetworkPreferences(
            allowLte = false
        )

    val preferences: StateFlow<WearPreferences> = dataStore.data.stateIn(
        coroutineScope, started = SharingStarted.Eagerly, WearPreferences()
    )
}