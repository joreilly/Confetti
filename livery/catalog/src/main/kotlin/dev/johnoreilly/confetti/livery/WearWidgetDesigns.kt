package dev.johnoreilly.confetti.livery

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import ee.schimke.composeai.rccreation.compose.layout.RemoteAlignment
import ee.schimke.composeai.rccreation.compose.layout.RemoteArrangement
import ee.schimke.composeai.rccreation.compose.layout.RemoteBox
import ee.schimke.composeai.rccreation.compose.layout.RemoteColumn
import ee.schimke.composeai.rccreation.compose.layout.RemoteRow
import ee.schimke.composeai.rccreation.compose.modifier.RemoteModifier
import ee.schimke.composeai.rccreation.compose.modifier.background
import ee.schimke.composeai.rccreation.compose.modifier.clickable
import ee.schimke.composeai.rccreation.compose.modifier.clip
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxSize
import ee.schimke.composeai.rccreation.compose.modifier.fillMaxWidth
import ee.schimke.composeai.rccreation.compose.modifier.padding
import ee.schimke.composeai.rccreation.compose.modifier.visibility
import ee.schimke.composeai.rccreation.compose.shapes.RemoteRoundedCornerShape
import ee.schimke.composeai.rccreation.compose.state.RemoteBoolean
import ee.schimke.composeai.rccreation.compose.state.RemoteColor
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.rf
import ee.schimke.composeai.rccreation.compose.state.rs
import ee.schimke.composeai.rcwidget.RcWearHost
import ee.schimke.composeai.rcwidget.creation.RcWearBackground
import ee.schimke.composeai.rcwidget.creation.RcWearBand
import ee.schimke.composeai.rcwidget.creation.RcWearEdge
import ee.schimke.composeai.rcwidget.creation.RcWearGlow
import ee.schimke.composeai.rcwidget.creation.RcWearTemplateContainer

/*
 * The session card as a Wear widget (Glance Wear, `wear-widget` targets): the next session the
 * user bookmarked, in each series' look. One design per container: a small one (the time and the
 * title) and a large one (with the room and the speakers).
 *
 * The container is the watch's: RcWearTemplateContainer reads its padding, corner radius and fill
 * from named values the watch writes in, so one capture fits a Pixel's squircle and a Galaxy's
 * stadium. What tells one conference from another at a glance is the container's background,
 * drawn inside the watch's own shape: Confetti's three accents as glows, droidcon's green rule and
 * outline, KotlinConf's brand gradient as glows and a gradient edge.
 *
 * A widget is drawn from a document with its theme baked in (the series' dark pack, a watch is
 * dark), so the backgrounds use the dark pack's colours directly; the text still reads the theme's
 * named roles, so a refreshed pack recolours it.
 */

/** A series' Wear widget: its container's background, and what goes before its times. */
class WearWidgetStyle(
    val brand: ConferenceBrand,
    val background: RcWearBackground,
    val timePrefix: String = "",
    /** The time's colour: the primary, unless that is the background's own (KotlinConf's purple). */
    val timeColor: (BrandColors) -> RemoteColor = { it.primary },
)

private fun Color.alpha(a: Float) = copy(alpha = a)

/** Confetti's own: its three accents (the dark pack's pink, orange and blue) as glows from the corners. */
val ConfettiWearWidget = WearWidgetStyle(
    brand = Confetti,
    background = RcWearBackground(
        base = Color(0xFF2D2530),
        layers = listOf(
            RcWearGlow(listOf(Color(0xFFFFA8FF).alpha(0.40f), Color.Transparent), x = 1.05f, y = -0.15f, radius = 0.55f),
            RcWearGlow(listOf(Color(0xFF5DD4FC).alpha(0.30f), Color.Transparent), x = -0.05f, y = 1.15f, radius = 0.45f),
            RcWearGlow(listOf(Color(0xFFFFB599).alpha(0.25f), Color.Transparent), x = 0.75f, y = 1.2f, radius = 0.3f),
        ),
    ),
)

/** droidcon: the site's near-black, a green rule down the start edge and a thin green outline. */
val DroidconWearWidget = WearWidgetStyle(
    brand = Droidcon,
    background = RcWearBackground(
        base = Color(0xFF0E1116),
        layers = listOf(
            RcWearGlow(listOf(Color(0xFF3DDC84).alpha(0.14f), Color.Transparent), x = 1f, y = 0f, radius = 0.6f),
            RcWearBand(Color(0xFF3DDC84), RcWearBand.Side.Start, thicknessDp = 4f),
        ),
        edge = RcWearEdge(listOf(Color(0xFF3DDC84).alpha(0.5f)), widthDp = 1f),
    ),
    timePrefix = "> ",
)

/** KotlinConf: the brand gradient (purple, magenta, coral) as glows, and round the edge. */
val KotlinConfWearWidget = WearWidgetStyle(
    brand = KotlinConf,
    background = RcWearBackground(
        base = Color(0xFF19191C),
        layers = listOf(
            RcWearGlow(listOf(Color(0xFF7F52FF).alpha(0.60f), Color.Transparent), x = 1f, y = 0f, radius = 0.8f),
            RcWearGlow(listOf(Color(0xFFC811E2).alpha(0.40f), Color.Transparent), x = 0f, y = 1f, radius = 0.6f),
            RcWearGlow(listOf(Color(0xFFE54857).alpha(0.30f), Color.Transparent), x = 1f, y = 1.1f, radius = 0.4f),
        ),
        edge = RcWearEdge(listOf(Color(0xFF7F52FF), Color(0xFFC811E2), Color(0xFFE54857), Color(0xFF7F52FF)), widthDp = 1.5f),
    ),
    // The purple glow is the primary: the time is the pack's lavender over it.
    timeColor = { it.muted },
)

/**
 * The session card (`confetti/session@1`) as a Wear widget in [style]. A tap opens the session; a
 * widget's taps carry no arguments the document builds, and `open` takes none (the app knows which
 * session the widget shows).
 */
@Composable
fun SessionWearWidget(style: WearWidgetStyle, size: WearWidgetSize) {
    val C = SessionContract
    val c = brandColors(style.brand)
    val time = style.timePrefix.rs + input(C.time)
    RcWearTemplateContainer(
        modifier = RemoteModifier.fillMaxSize().clickable(emit(C.open)),
        background = style.background,
        defaults = if (size == WearWidgetSize.Large) RcWearHost.Default.large else RcWearHost.Default.small,
    ) {
        when (size) {
            // 60 dp tall: the time over the title, one line each.
            WearWidgetSize.Small -> RemoteColumn(
                modifier = RemoteModifier.fillMaxSize().padding(horizontal = 6.rdp),
                verticalArrangement = RemoteArrangement.Center,
            ) {
                Label(time, style.timeColor(c), size = 12, weight = FontWeight.SemiBold)
                Label(input(C.title), c.onSurface, size = 14, weight = FontWeight.Bold)
            }
            // The time (and whether it's on now), the title on up to two lines, the room, the speakers.
            WearWidgetSize.Large -> RemoteColumn(
                modifier = RemoteModifier.fillMaxSize().padding(horizontal = 6.rdp),
                verticalArrangement = RemoteArrangement.spacedBy(1.rdp, RemoteAlignment.CenterVertically),
            ) {
                RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically, horizontalArrangement = RemoteArrangement.spacedBy(6.rdp)) {
                    Label(time, style.timeColor(c), size = 12, weight = FontWeight.SemiBold)
                    NowPill(inputBool(C.live), c.primary, c.onPrimary)
                }
                Label(input(C.title), c.onSurface, size = 15, weight = FontWeight.Bold, maxLines = 2)
                Label(input(C.room), c.muted, size = 12, weight = FontWeight.Medium)
                Label(input(C.speakers), c.muted, size = 12, weight = FontWeight.Normal)
            }
        }
    }
}

/** "Now", while the session runs: still, as a widget has no clock to pulse with. */
@Composable
private fun NowPill(live: RemoteBoolean, background: RemoteColor, content: RemoteColor) {
    RemoteBox(
        modifier = RemoteModifier
            .visibility(live.visibleIf())
            .clip(RemoteRoundedCornerShape(50.rdp))
            .background(background)
            .padding(horizontal = 6.rdp, vertical = 1.rdp),
    ) {
        Label("Now".rs, content, size = 10)
    }
}
