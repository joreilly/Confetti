@file:Suppress("DEPRECATION")

package dev.johnoreilly.confetti.wear.tile

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.wear.compose.ui.tooling.preview.WearPreviewLargeRound
import androidx.wear.protolayout.ActionBuilders.LoadAction
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography.TYPOGRAPHY_BODY1
import androidx.wear.protolayout.material.Typography.TYPOGRAPHY_TITLE2
import androidx.wear.protolayout.material.layouts.MultiSlotLayout
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.CurrentSessionsData
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.NoConference
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.NotLoggedIn
import dev.johnoreilly.confetti.wear.ui.toTileColors
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.wear.compose.material.Colors as WearComposeColors

class CurrentSessionsTileRenderer(
    context: Context
) :
    SingleTileLayoutRenderer<ConfettiTileData, ConfettiTileData>(context) {
    val colors = MutableStateFlow(WearComposeColors())

    override fun createTheme(): androidx.wear.protolayout.material.Colors =
        colors.value.toTileColors()

    override fun renderTile(
        state: ConfettiTileData,
        deviceParameters: DeviceParameters
    ): LayoutElementBuilders.LayoutElement = when (state) {
        is CurrentSessionsData -> renderBookmarksTile(state, deviceParameters)
        is NotLoggedIn -> renderLoginTile(state, deviceParameters)
        is NoConference -> renderNoConferenceTile(state, deviceParameters)
    }

    override fun getResourcesVersionForTileState(state: ConfettiTileData): String {
        return (state as? CurrentSessionsData)?.bookmarks.orEmpty()
            .take(3)
            .joinToString(":") { it.id }
    }

    fun renderBookmarksTile(
        state: CurrentSessionsData,
        deviceParameters: DeviceParameters
    ): LayoutElementBuilders.LayoutElement = PrimaryLayout.Builder(deviceParameters)
        .setPrimaryLabelTextContent(
            conferenceLabel(state.conference.name)
        )
        .setContent(
            sessionsList(state, state.bookmarks, deviceParameters)
        )
        .setPrimaryChipContent(
            CompactChip.Builder(
                context,
                "Bookmarks",
                browseClickable(state.conference.id),
                deviceParameters
            )
                .setChipColors(ChipColors.primaryChipColors(theme))
                .build()
        )
        .setResponsiveContentInsetEnabled(true)
        .build()

    fun renderLoginTile(
        state: NotLoggedIn,
        deviceParameters: DeviceParameters
    ): LayoutElementBuilders.LayoutElement = PrimaryLayout.Builder(deviceParameters)
        .setPrimaryLabelTextContent(
            conferenceLabel(state.conference?.name ?: "Confetti")
        )
        .setContent(
            message("Not Logged In")
        )
        .setPrimaryChipContent(
            CompactChip.Builder(
                context,
                "Login",
                loginClickable(),
                deviceParameters
            )
                .setChipColors(ChipColors.primaryChipColors(theme))
                .build()
        )
        .setResponsiveContentInsetEnabled(true)
        .build()

    fun renderNoConferenceTile(
        state: NoConference,
        deviceParameters: DeviceParameters
    ): LayoutElementBuilders.LayoutElement = PrimaryLayout.Builder(deviceParameters)
        .setPrimaryLabelTextContent(
            conferenceLabel("Confetti")
        )
        .setContent(
            message("No Conference Selected")
        )
        .setPrimaryChipContent(
            CompactChip.Builder(
                context,
                "Conferences",
                conferencesClickable(),
                deviceParameters
            )
                .setChipColors(ChipColors.primaryChipColors(theme))
                .build()
        )
        .setResponsiveContentInsetEnabled(true)
        .build()

    fun conferenceLabel(state: String) = Text.Builder(context, state)
        .setTypography(TYPOGRAPHY_TITLE2)
        .setColor(ColorBuilders.argb(theme.primary))
        .setMaxLines(2)
        .build()

    fun message(state: String) = Text.Builder(context, state)
        .setTypography(TYPOGRAPHY_BODY1)
        .setColor(ColorBuilders.argb(theme.primary))
        .build()

    fun sessionsList(
        state: CurrentSessionsData,
        sessions: List<SessionDetails>,
        deviceParameters: DeviceParameters
    ) =
        MultiSlotLayout.Builder()
            .apply {
                sessions.take(3).forEach {
                    addSlotContent(sessionChip(state, it, deviceParameters))
                }
            }
            .build()

    private fun sessionChip(
        state: CurrentSessionsData,
        sessionDetails: SessionDetails,
        deviceParameters: DeviceParameters
    ): LayoutElementBuilders.LayoutElement =
        Chip.Builder(
            context,
            sessionClickable(state.conference.id, sessionDetails),
            deviceParameters
        )
            .setChipColors(ChipColors.secondaryChipColors(theme))
            .setPrimaryLabelContent(sessionDetails.title)
            .setSecondaryLabelContent(sessionDetails.room?.name ?: "No Room")
            .build()

    private fun sessionClickable(
        conference: String,
        sessionDetails: SessionDetails
    ): Clickable =
        Clickable.Builder()
            .setOnClick(LoadAction.Builder().build())
            .setId("session/${conference}/${sessionDetails.id}")
            .build()

    private fun browseClickable(
        conference: String
    ): Clickable =
        Clickable.Builder()
            .setOnClick(LoadAction.Builder().build())
            .setId("conferenceHome/${conference}")
            .build()

    private fun conferencesClickable(
    ): Clickable =
        Clickable.Builder()
            .setOnClick(LoadAction.Builder().build())
            .setId("conferences")
            .build()

    private fun loginClickable(
    ): Clickable =
        Clickable.Builder()
            .setOnClick(LoadAction.Builder().build())
            .setId("signIn")
            .build()
}