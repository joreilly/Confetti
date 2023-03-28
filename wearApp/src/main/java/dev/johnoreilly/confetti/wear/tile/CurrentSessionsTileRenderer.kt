@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.tile

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.wear.tiles.ActionBuilders.AndroidActivity
import androidx.wear.tiles.ActionBuilders.AndroidStringExtra
import androidx.wear.tiles.ActionBuilders.LaunchAction
import androidx.wear.tiles.ColorBuilders.argb
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
import dev.johnoreilly.confetti.type.Session
import dev.johnoreilly.confetti.wear.MainActivity
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewDevices
import dev.johnoreilly.confetti.wear.ui.previews.WearPreviewFontSizes
import dev.johnoreilly.confetti.wear.ui.toTileColors
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class CurrentSessionsTileRenderer(
    context: Context
) :
    SingleTileLayoutRenderer<CurrentSessionsData, CurrentSessionsData>(context) {
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    private var colors: androidx.wear.compose.material.Colors =
        androidx.wear.compose.material.Colors()

    override fun createTheme(): Colors = colors.toTileColors()

    override fun renderTile(
        state: CurrentSessionsData,
        deviceParameters: DeviceParameters
    ): LayoutElementBuilders.LayoutElement = PrimaryLayout.Builder(deviceParameters)
        .setPrimaryLabelTextContent(
            timeLabel(state)
        )
        .setContent(
            sessionsList(state, state.sessions, deviceParameters)
        )
        .setPrimaryChipContent(
            CompactChip.Builder(
                context,
                "Browse",
                browseClickable(state.conference),
                deviceParameters
            )
                .setChipColors(ChipColors.primaryChipColors(theme))
                .build()
        )
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

    fun timeLabel(state: CurrentSessionsData) = Text.Builder(
        context,
        state.sessionTime?.toJavaLocalDateTime()?.toLocalTime()?.format(timeFormatter)
            ?: "None"
    )
        .setTypography(Typography.TYPOGRAPHY_CAPTION1)
        .setColor(argb(theme.primary))
        .build()

    private fun sessionChip(
        state: CurrentSessionsData,
        sessionDetails: SessionDetails,
        deviceParameters: DeviceParameters
    ): LayoutElementBuilders.LayoutElement =
        Chip.Builder(context, sessionClickable(state.conference, sessionDetails), deviceParameters)
            .setChipColors(ChipColors.secondaryChipColors(theme))
            .setPrimaryLabelContent(sessionDetails.title)
            .setSecondaryLabelContent(sessionDetails.room?.name ?: "No Room")
            .build()

    private fun sessionClickable(
        conference: String,
        sessionDetails: SessionDetails
    ): Clickable =
        Clickable.Builder()
            .setOnClick(
                LaunchAction.Builder()
                    .setAndroidActivity(
                        AndroidActivity.Builder()
                            .setClassName(MainActivity::class.java.name)
                            .setPackageName(context.packageName)
                            .addKeyToExtraMapping(
                                "session", AndroidStringExtra.Builder()
                                    .setValue(sessionDetails.id)
                                    .build()
                            )
                            .addKeyToExtraMapping(
                                "conference", AndroidStringExtra.Builder()
                                    .setValue(conference)
                                    .build()
                            )
                            .addKeyToExtraMapping(
                                "tile", AndroidStringExtra.Builder()
                                    .setValue("session")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()

    private fun browseClickable(
        conference: String
    ): Clickable =
        Clickable.Builder()
            .setOnClick(
                LaunchAction.Builder()
                    .setAndroidActivity(
                        AndroidActivity.Builder()
                            .setClassName(MainActivity::class.java.name)
                            .setPackageName(context.packageName)
                            .addKeyToExtraMapping(
                                "conference", AndroidStringExtra.Builder()
                                    .setValue(conference)
                                    .build()
                            )
                            .addKeyToExtraMapping(
                                "tile", AndroidStringExtra.Builder()
                                    .setValue("browse")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()

    fun updateTheme(theme: androidx.wear.compose.material.Colors) {
        this.colors = theme
    }
}

@WearPreviewDevices
@WearPreviewFontSizes
@Composable
fun CurrentSessionsTilePreview() {
    val context = LocalContext.current

    val sessionTime = LocalDateTime(2022, 12, 25, 12, 30)

    val tileState = remember {
        CurrentSessionsData(
            "wearconf",
            sessionTime,
            listOf(
                SessionDetails(
                    "1",
                    "Wear it's at",
                    "Talk",
                    sessionTime,
                    sessionTime,
                    "Be aWear of what's coming",
                    "en",
                    listOf(),
                    SessionDetails.Room("Main Hall"),
                    listOf(),
                    Session.type.name
                )
            )
        )
    }
    val renderer = remember { CurrentSessionsTileRenderer(context) }

    TileLayoutPreview(
        tileState, tileState, renderer
    )
}