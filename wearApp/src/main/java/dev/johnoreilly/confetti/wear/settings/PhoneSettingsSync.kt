package dev.johnoreilly.confetti.wear.settings

import androidx.compose.ui.graphics.Color
import com.google.android.horologist.data.ProtoDataStoreHelper.protoFlow
import com.google.android.horologist.data.TargetNodeId.PairedPhone
import com.google.android.horologist.data.WearDataLayerRegistry
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.wear.proto.WearSettings
import dev.johnoreilly.confetti.wear.ui.toColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEmpty

class PhoneSettingsSync(
    dataLayerRegistry: WearDataLayerRegistry,
    val repository: ConfettiRepository
) {
    val settingsFlow = dataLayerRegistry.protoFlow<WearSettings>(PairedPhone).onEmpty {
        emit(WearSettings())
    }.catch {
        // Fails on robolectric
        emit(WearSettings())
    }

    val conferenceFlow: Flow<ConferenceSelection> = combine(
        settingsFlow,
        repository.getConferenceFlow()
    ) { phoneSettings, conferenceSetting ->
        if (phoneSettings.conference.isNotBlank()) {
            ConferenceSelection(phoneSettings.conference, phoneSettings.color_scheme?.toColor())
        } else {
            ConferenceSelection(conferenceSetting)
        }
    }
}

data class ConferenceSelection(
    val conference: String,
    val colorScheme: Color? = null
)

