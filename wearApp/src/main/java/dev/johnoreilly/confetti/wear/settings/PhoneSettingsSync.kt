@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.settings

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.data.ProtoDataStoreHelper.protoFlow
import com.google.android.horologist.data.TargetNodeId.PairedPhone
import com.google.android.horologist.data.WearDataLayerRegistry
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.wear.proto.Theme
import dev.johnoreilly.confetti.wear.proto.WearSettings
import kotlinx.coroutines.flow.combine

class PhoneSettingsSync(
    val dataLayerRegistry: WearDataLayerRegistry,
    val repository: ConfettiRepository
) {
    val settingsFlow = dataLayerRegistry.protoFlow<WearSettings>(PairedPhone)

    val conferenceFlow = combine(
        settingsFlow,
        repository.getConferenceFlow()
    ) { phoneSettings, conferenceSetting ->
        phoneSettings.conference.ifBlank { conferenceSetting }
    }
}

fun Theme.toMaterialThemeColors(): Colors {
    return Colors(
        primary = toColor(primary),
        primaryVariant = toColor(primaryVariant),
        secondary = toColor(secondary),
        secondaryVariant = toColor(secondaryVariant),
        surface = toColor(surface),
        error = toColor(error),
        onPrimary = toColor(onPrimary),
        onSecondary = toColor(onSecondary),
        onBackground = toColor(onBackground),
        onSurface = toColor(onSurface),
        onSurfaceVariant = toColor(onSurfaceVariant),
        onError = toColor(onError),
    )
}

private fun toColor(primary1: Long) = Color(primary1.toULong())