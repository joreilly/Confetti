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
import kotlinx.coroutines.flow.onEmpty

class PhoneSettingsSync(
    val dataLayerRegistry: WearDataLayerRegistry,
    val repository: ConfettiRepository
) {
    val settingsFlow = dataLayerRegistry.protoFlow<WearSettings>(PairedPhone).onEmpty {
        emit(WearSettings())
    }

    val conferenceFlow = combine(
        settingsFlow,
        repository.getConferenceFlow()
    ) { phoneSettings, conferenceSetting ->
        phoneSettings.conference.ifBlank { conferenceSetting }
    }
}

fun Theme.toMaterialThemeColors(): Colors {
    return Colors(
        primary = Color(primary),
        primaryVariant = Color(primaryVariant),
        secondary = Color(secondary),
        secondaryVariant = Color(secondaryVariant),
        surface = Color(surface),
        error = Color(error),
        onPrimary = Color(onPrimary),
        onSecondary = Color(onSecondary),
        onBackground = Color(onBackground),
        onSurface = Color(onSurface),
        onSurfaceVariant = Color(onSurfaceVariant),
        onError = Color(onError),
    )
}

