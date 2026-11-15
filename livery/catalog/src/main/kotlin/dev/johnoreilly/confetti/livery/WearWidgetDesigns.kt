package dev.johnoreilly.confetti.livery

import ee.schimke.composeai.rcwidget.creation.RcBreakpointAxis
import ee.schimke.composeai.rcwidget.RcWearContainer
import ee.schimke.composeai.rccreation.compose.state.RemoteState
import ee.schimke.composeai.rccreation.compose.state.RemotePaint
import ee.schimke.composeai.rccreation.compose.state.RemoteFloat
import ee.schimke.composeai.rccreation.compose.state.RemoteDp
import ee.schimke.composeai.rccreation.compose.modifier.drawWithContent
import ee.schimke.composeai.rccreation.compose.layout.RemoteSize
import ee.schimke.composeai.rccreation.compose.layout.RemoteOffset
import ee.schimke.composeai.rccreation.compose.layout.RemoteDrawScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import ee.schimke.composeai.rccreation.compose.text.RemoteTextAlign
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
import ee.schimke.composeai.rcwidget.creation.RcSizeBreakpoint
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
    /** What the series draws in the container beside its background's layers. */
    val art: WearWidgetArt = WearWidgetArt.None,
)

/**
 * A series' illustration in its widget's container, as its Wear designs draw it (WearBackdrops.kt
 * in :livery:wear-designs): along the container's top and bottom edges, in its vertical padding, so
 * it stays clear of the text and inside a stadium's flat edges as well as a squircle's.
 */
enum class WearWidgetArt {
    None,

    /** Confetti in the theme's three accents, scattered along the top and bottom edges. */
    Confetti,

    /** droidcon's dot matrix, as a row along the bottom edge fading out to either side. */
    DotRow,
}

private fun Color.alpha(a: Float) = copy(alpha = a)

/** Confetti's own: its three accents (the dark pack's pink, orange and blue) as glows from the corners. */
val ConfettiWearWidget = WearWidgetStyle(
    brand = Confetti,
    art = WearWidgetArt.Confetti,
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
    art = WearWidgetArt.DotRow,
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
    val defaults = if (size == WearWidgetSize.Large) RcWearHost.Default.large else RcWearHost.Default.small
    // The watch's own corner radius (the same named value the container reads), so the illustration
    // is cut to the host's shape: a squircle's 26 dp, a stadium's 999.
    val corner = RemoteDp.createNamedRemoteDp(RcWearContainer.CORNER_RADIUS, defaults.cornerRadiusDp, RemoteState.Domain.User)
    val base = RemoteColor(style.background.base)
    RcWearTemplateContainer(
        modifier = RemoteModifier
            .fillMaxSize()
            .clip(RemoteRoundedCornerShape(corner))
            // The background's base, then the illustration, then the container's own layers, edge
            // and content over them (its fill left clear, below).
            .drawWithContent {
                drawRect(RemotePaint { color = base })
                art(style.art, c)
                drawContent()
            }
            .clickable(emit(C.open)),
        background = style.background.copy(base = Color.Transparent),
        defaults = defaults,
    ) {
        when (size) {
            // 60 dp tall: the time over the title, one line each.
            WearWidgetSize.Small -> RemoteColumn(
                modifier = RemoteModifier.fillMaxSize().padding(horizontal = 2.rdp),
                verticalArrangement = RemoteArrangement.Center,
            ) {
                Label(style.timePrefix.rs + input(C.time), style.timeColor(c), size = 12, weight = FontWeight.SemiBold)
                Label(input(C.title), c.onSurface, size = 14, weight = FontWeight.Bold)
            }
            // The content's size inside the container's padding is the watch's, and Glance Wear's own
            // frames (wear-tooling-preview) run from a squircle's 184×92 dp (240 dp screen) and
            // 150×80 (204 dp) to a Samsung stadium's 90×104 (240 dp) and 92×88 (216 dp). Narrower
            // than 150 dp, "Now" and the time can't share a line and the title wants a third; shorter
            // than the four lines need, the speakers (wide) or the title's third line (narrow) go.
            WearWidgetSize.Large -> RcSizeBreakpoint(intArrayOf(NARROW_DP), RemoteModifier.fillMaxSize()) { wide ->
                if (wide == 0) {
                    RcSizeBreakpoint(intArrayOf(NARROW_TALL_DP), RemoteModifier.fillMaxSize(), RcBreakpointAxis.Height) { tall ->
                        NarrowLarge(style, c, titleLines = if (tall == 0) 2 else 3)
                    }
                } else {
                    RcSizeBreakpoint(intArrayOf(WIDE_TALL_DP), RemoteModifier.fillMaxSize(), RcBreakpointAxis.Height) { tall ->
                        WideLarge(style, c, speakers = tall == 1)
                    }
                }
            }
        }
    }
}

/** Below this content width a large container gets the narrow layout. */
private const val NARROW_DP = 150

/** The content height a wide layout needs for the speakers' line too (a squircle's 92 dp, not 80). */
private const val WIDE_TALL_DP = 90

/** The content height a narrow layout needs for a three-line title (a stadium's 104 dp, not 88). */
private const val NARROW_TALL_DP = 100

/** The time (and whether it's on now), the title on up to two lines, the room, and the [speakers]. */
@Composable
private fun WideLarge(style: WearWidgetStyle, c: BrandColors, speakers: Boolean) {
    val C = SessionContract
    RemoteColumn(
        modifier = RemoteModifier.fillMaxSize().padding(horizontal = 2.rdp),
        verticalArrangement = RemoteArrangement.spacedBy(1.rdp, RemoteAlignment.CenterVertically),
    ) {
        RemoteRow(verticalAlignment = RemoteAlignment.CenterVertically, horizontalArrangement = RemoteArrangement.spacedBy(6.rdp)) {
            val live = inputBool(C.live)
            NowPill(live, c.primary, c.onPrimary)
            // "Now" stands in for the series' prefix, so the time keeps its line at 150 dp.
            Label(live.select("".rs, style.timePrefix.rs) + input(C.time), style.timeColor(c), size = 12, weight = FontWeight.SemiBold)
        }
        Label(input(C.title), c.onSurface, size = 15, weight = FontWeight.Bold, maxLines = 2)
        Label(input(C.room), c.muted, size = 12, weight = FontWeight.Medium)
        if (speakers) Label(input(C.speakers), c.muted, size = 12, weight = FontWeight.Normal)
    }
}

/**
 * A narrow large container (a Samsung's): centred, "Now" over the time, the title on up to
 * [titleLines] lines, then the room. The time drops the series' prefix to fit on its line.
 */
@Composable
private fun NarrowLarge(style: WearWidgetStyle, c: BrandColors, titleLines: Int) {
    val C = SessionContract
    RemoteColumn(
        modifier = RemoteModifier.fillMaxSize(),
        horizontalAlignment = RemoteAlignment.CenterHorizontally,
        verticalArrangement = RemoteArrangement.Center,
    ) {
        NowPill(inputBool(C.live), c.primary, c.onPrimary)
        Label(input(C.time), style.timeColor(c), size = 11, weight = FontWeight.SemiBold)
        Label(input(C.title), c.onSurface, size = 14, weight = FontWeight.Bold, maxLines = titleLines, align = RemoteTextAlign.Center)
        Label(input(C.room), c.muted, size = 11, weight = FontWeight.Medium)
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

/** [art] across the whole container, in the theme's accents. */
private fun RemoteDrawScope.art(art: WearWidgetArt, c: BrandColors) {
    when (art) {
        WearWidgetArt.None -> {}
        WearWidgetArt.Confetti -> {
            // A garland along each edge, centred: inside a stadium's flat edges too.
            val top = 4.5f.rdp.toPx()
            val bottom = height - 4.5f.rdp.toPx()
            piece(c.primary, 0.30f, top, strip = true, angle = 35f)
            piece(c.tertiary, 0.40f, top, strip = false)
            piece(c.secondary, 0.50f, top, strip = true, angle = -20f)
            piece(c.primary, 0.60f, top, strip = false)
            piece(c.tertiary, 0.70f, top, strip = true, angle = 60f)
            piece(c.secondary, 0.35f, bottom, strip = false)
            piece(c.primary, 0.45f, bottom, strip = true, angle = -40f)
            piece(c.tertiary, 0.55f, bottom, strip = false)
            piece(c.secondary, 0.65f, bottom, strip = true, angle = 25f)
        }
        WearWidgetArt.DotRow -> {
            val y = height - 4.5f.rdp.toPx()
            val step = 7.rdp.toPx()
            for (i in -5..5) {
                val alpha = 0.55f - 0.1f * kotlin.math.abs(i)
                if (alpha <= 0f) continue
                drawCircle(RemotePaint { color = c.primary.copy(alpha = alpha.rf) }, 1.2f.rdp.toPx(), RemoteOffset(width / 2f + step * i.toFloat(), y))
            }
        }
    }
}

/** A piece of confetti at [x] (a fraction of the width) and [y]: a dot, or a strip turned by [angle]. */
private fun RemoteDrawScope.piece(color: RemoteColor, x: Float, y: RemoteFloat, strip: Boolean, angle: Float = 0f) {
    val cx = width * x
    val paint = RemotePaint { this.color = color.copy(alpha = 0.85f.rf) }
    if (!strip) {
        drawCircle(paint, 2.2f.rdp.toPx(), RemoteOffset(cx, y))
    } else {
        val w = 7.rdp.toPx()
        val h = 2.6f.rdp.toPx()
        rotate(angle.rf, RemoteOffset(cx, y)) {
            drawRect(paint, RemoteOffset(cx - w / 2f, y - h / 2f), RemoteSize(w, h))
        }
    }
}
