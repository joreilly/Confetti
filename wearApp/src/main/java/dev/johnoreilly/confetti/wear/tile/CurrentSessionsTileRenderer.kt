@file:Suppress("DEPRECATION")

package dev.johnoreilly.confetti.wear.tile

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.protolayout.ActionBuilders.LoadAction
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.TypeBuilders.StringProp
import androidx.wear.protolayout.material.Chip
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.layouts.MultiSlotLayout
import androidx.wear.protolayout.material3.*
import androidx.wear.protolayout.types.argb
import androidx.wear.protolayout.types.layoutString
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.CurrentSessionsData
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.NoConference
import dev.johnoreilly.confetti.wear.tile.ConfettiTileData.NotLoggedIn
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.wear.compose.material3.ColorScheme as WearComposeColors

class CurrentSessionsTileRenderer(
    context: Context
) :
    SingleTileLayoutRenderer<ConfettiTileData, ConfettiTileData>(context) {
    val colors = MutableStateFlow(WearComposeColors())

    // TODO
//    override fun createTheme(): androidx.wear.protolayout.material.Colors =
//        colors.value.toTileColors()

    override fun renderTile(
        state: ConfettiTileData,
        deviceParameters: DeviceParameters
    ): LayoutElementBuilders.LayoutElement =
        materialScope(context, deviceParameters) {
            when (state) {
                is CurrentSessionsData -> renderBookmarksTile(state)
                is NotLoggedIn -> renderLoginTile(state)
                is NoConference -> renderNoConferenceTile()
            }
        }

    override fun getResourcesVersionForTileState(state: ConfettiTileData): String {
        return (state as? CurrentSessionsData)?.bookmarks.orEmpty()
            .take(3)
            .joinToString(":") { it.id }
    }

    fun MaterialScope.renderBookmarksTile(
        state: CurrentSessionsData,
    ): LayoutElementBuilders.LayoutElement = primaryLayout(
        mainSlot = {
            sessionsList(state, state.bookmarks)
        },
        titleSlot = { conferenceLabel(state.conference.name) },
        bottomSlot = {
            textEdgeButton(
                onClick = browseClickable(state.conference.id),
                labelContent = { text("Bookmarks".layoutString) })
        })

    fun MaterialScope.renderLoginTile(
        state: NotLoggedIn,
    ): LayoutElementBuilders.LayoutElement = primaryLayout(
        mainSlot = {
            text("Not Logged In".layoutString)
        },
        titleSlot = { conferenceLabel(state.conference?.name ?: "Confetti") },
        bottomSlot = {
            textEdgeButton(
                onClick = loginClickable(),
                labelContent = { text("Login".layoutString) })
        })

    fun MaterialScope.renderNoConferenceTile(
    ): LayoutElementBuilders.LayoutElement = primaryLayout(
        mainSlot = {
            text("No Conference Selected".layoutString)
        },
        titleSlot = { conferenceLabel("Confetti") },
        bottomSlot = {
            textEdgeButton(
                onClick = conferencesClickable(),
                labelContent = { text("Conferences".layoutString) })
        })

    fun MaterialScope.conferenceLabel(state: String) =
        text(state.layoutString, typography = Typography.TITLE_MEDIUM, color = theme.primary.argb, maxLines = 2)

    fun MaterialScope.message(state: String) =
        text(state.layoutString, typography = Typography.BODY_LARGE, color = theme.primary.argb)

    fun MaterialScope.sessionsList(
        state: CurrentSessionsData,
        sessions: List<SessionDetails>,
    ) =
        MultiSlotLayout.Builder()
            .apply {
                sessions.take(3).forEach {
                    addSlotContent(sessionChip(state, it))
                }
            }
            .build()

    fun string(text: String) = StringProp.Builder(text)
        .build()

    fun color(color: Color) = ColorBuilders.ColorProp.Builder(color.toArgb())
        .build()

    fun color(color: Int) = ColorBuilders.ColorProp.Builder(color)
        .build()

    private fun MaterialScope.sessionChip(
        state: CurrentSessionsData,
        sessionDetails: SessionDetails,
    ): LayoutElementBuilders.LayoutElement =
        // TODO material3 Button?
        Chip.Builder(
            context,
            sessionClickable(state.conference.id, sessionDetails),
            this.deviceConfiguration
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