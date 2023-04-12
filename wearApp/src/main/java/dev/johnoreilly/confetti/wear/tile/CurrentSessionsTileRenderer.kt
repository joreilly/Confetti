@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.tile

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.wear.tiles.ActionBuilders.AndroidActivity
import androidx.wear.tiles.ActionBuilders.AndroidStringExtra
import androidx.wear.tiles.ActionBuilders.LaunchAction
import androidx.wear.tiles.ActionBuilders.LoadAction
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.material.Chip
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.Colors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.material.layouts.MultiSlotLayout
import androidx.wear.tiles.material.layouts.PrimaryLayout
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.tools.TileLayoutPreview
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.MainActivity
import dev.johnoreilly.confetti.wear.preview.TestFixtures
import dev.johnoreilly.confetti.wear.sessions.navigation.SessionsDestination
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.*
import dev.johnoreilly.confetti.wear.ui.previews.WearLargeRoundDevicePreview
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import dev.johnoreilly.confetti.wear.ui.toTileColors
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.wear.compose.material.Colors as WearComposeColors

class CurrentSessionsTileRenderer(
    context: Context
) :
    SingleTileLayoutRenderer<ConfettiTileData, ConfettiTileData>(context) {
    val colors = MutableStateFlow(WearComposeColors())

    override fun createTheme(): Colors = colors.value.toTileColors()

    override fun renderTile(
        state: ConfettiTileData,
        deviceParameters: DeviceParameters
    ): LayoutElementBuilders.LayoutElement = when (state) {
        is CurrentSessionsData -> renderBookmarksTile(state, deviceParameters)
        is NotLoggedIn -> renderLoginTile(state, deviceParameters)
        is NoConference -> renderNoConferenceTile(state, deviceParameters)
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
        .build()

    fun conferenceLabel(state: String) = Text.Builder(context, state)
        .setTypography(Typography.TYPOGRAPHY_TITLE2)
        .setColor(ColorBuilders.argb(theme.primary))
        .build()

    fun message(state: String) = Text.Builder(context, state)
        .setTypography(Typography.TYPOGRAPHY_BODY1)
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
            .setId("session/{${conference}}/{${sessionDetails.id}}")
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

@WearLargeRoundDevicePreview
@Composable
fun LoginTilePreview() {
    val context = LocalContext.current

    val tileState = remember {
        NotLoggedIn(
            TestFixtures.kotlinConf2023Config
        )
    }
    val renderer = remember { CurrentSessionsTileRenderer(context) }

    TileLayoutPreview(
        tileState, tileState, renderer
    )
}

@WearLargeRoundDevicePreview
@Composable
fun NoConferenceTilePreview() {
    val context = LocalContext.current

    val tileState = remember {
        NoConference
    }
    val renderer = remember { CurrentSessionsTileRenderer(context) }

    TileLayoutPreview(
        tileState, tileState, renderer
    )
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun BookmarksTilePreview() {
    val context = LocalContext.current

    val tileState = remember {
        CurrentSessionsData(
            TestFixtures.kotlinConf2023Config,
            listOf(
                TestFixtures.sessionDetails
            )
        )
    }
    val renderer = remember { CurrentSessionsTileRenderer(context) }

    TileLayoutPreview(
        tileState, tileState, renderer
    )
}