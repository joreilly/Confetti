package dev.johnoreilly.confetti.livery.wear

import ee.schimke.composeai.rccreation.compose.layout.RemoteDrawScope
import ee.schimke.composeai.rccreation.compose.layout.RemoteOffset
import ee.schimke.composeai.rccreation.compose.layout.RemoteSize
import ee.schimke.composeai.rccreation.compose.shaders.RemoteBrush
import ee.schimke.composeai.rccreation.compose.shaders.linearGradient
import ee.schimke.composeai.rccreation.compose.shaders.radialGradient
import ee.schimke.composeai.rccreation.compose.shaders.solidColor
import ee.schimke.composeai.rccreation.compose.shaders.sweepGradient
import ee.schimke.composeai.rccreation.compose.state.RemoteColor
import ee.schimke.composeai.rccreation.compose.state.RemoteFloat
import ee.schimke.composeai.rccreation.compose.state.RemotePaint
import ee.schimke.composeai.rccreation.compose.state.rdp
import ee.schimke.composeai.rccreation.compose.state.rf
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ee.schimke.composeai.rccreation.wear.material3.RemoteBorderStroke
import ee.schimke.composeai.rccreation.wear.material3.RemoteMaterialTheme

/*
 * What sets each series apart on a watch beyond its colours: a backdrop drawn under a card's or a
 * header's content, clipped to its shape, after each conference's site.
 *
 *  - Confetti: confetti, scattered in the theme's three accents.
 *  - droidcon: the site's dark panels with a green rule down the side and a dot matrix.
 *  - KotlinConf: the brand gradient (purple, magenta, coral) as glows and the cards' outline.
 *
 * Everything is drawn from the theme's named colours and the slot's size as the document plays, so
 * a theme pack recolours it and it fits any watch.
 */

/** The theme colours a backdrop draws with, read once in composition. */
class BackdropColors(
    val primary: RemoteColor,
    val secondary: RemoteColor,
    val tertiary: RemoteColor,
    val surface: RemoteColor,
)

@Composable
fun backdropColors(): BackdropColors {
    val c = RemoteMaterialTheme.colorScheme
    return BackdropColors(c.primary, c.secondary, c.tertiary, c.surfaceContainer)
}

/** No colour: what a card's own container draws once the backdrop has filled it. */
val Transparent: RemoteColor = RemoteColor(Color.Transparent)

/** A card's outline for [style]: KotlinConf's gradient, droidcon's green, none for Confetti. */
fun outlineFor(style: WearStyle, colors: BackdropColors): RemoteBorderStroke? = when (style) {
    WearStyle.Confetti -> null
    WearStyle.Droidcon -> RemoteBorderStroke(1.rdp, RemoteBrush.solidColor(colors.primary.copy(alpha = 0.6f.rf)))
    WearStyle.KotlinConf -> RemoteBorderStroke(
        1.5f.rdp,
        RemoteBrush.linearGradient(listOf(colors.primary, colors.secondary, colors.tertiary)),
    )
}

/** The brush a speaker's photo ring is drawn with. */
fun ringBrush(style: WearStyle, colors: BackdropColors, center: RemoteOffset? = null): RemoteBrush = when (style) {
    WearStyle.Confetti -> RemoteBrush.sweepGradient(
        listOf(colors.primary, colors.secondary, colors.tertiary, colors.primary),
        center,
    )
    WearStyle.Droidcon -> RemoteBrush.solidColor(colors.primary)
    WearStyle.KotlinConf -> RemoteBrush.linearGradient(listOf(colors.primary, colors.secondary, colors.tertiary))
}

/**
 * Draws [style]'s backdrop over the whole of this scope: the container colour when [fill] (cards
 * and header panels), then the series' marks.
 */
fun RemoteDrawScope.backdrop(style: WearStyle, colors: BackdropColors, fill: Boolean = true) {
    if (fill) drawRect(paint(colors.surface))
    when (style) {
        WearStyle.Confetti -> confetti(colors)
        WearStyle.Droidcon -> droidconPanel(colors)
        WearStyle.KotlinConf -> kotlinConfGlow(colors)
    }
}

/** A section header's marks: lighter than a card's, on the watch's background. */
fun RemoteDrawScope.sectionMarks(style: WearStyle, colors: BackdropColors) {
    when (style) {
        // A few pieces either side of the title.
        WearStyle.Confetti -> {
            piece(colors.primary, fromLeft = 10f, y = 0.35f, kind = Piece.Strip, angle = 30f)
            piece(colors.tertiary, fromLeft = 20f, y = 0.7f, kind = Piece.Dot)
            piece(colors.secondary, fromRight = 12f, y = 0.3f, kind = Piece.Dot)
            piece(colors.primary, fromRight = 22f, y = 0.65f, kind = Piece.Strip, angle = -40f)
        }
        // A short green rule at the start, as the site's section labels have.
        WearStyle.Droidcon -> drawRect(
            paint(colors.primary),
            RemoteOffset(4.rdp.toPx(), height / 2f - 1.rdp.toPx()),
            RemoteSize(10.rdp.toPx(), 2.rdp.toPx()),
        )
        // The brand gradient as an underline, fading out at both ends.
        WearStyle.KotlinConf -> drawRect(
            brushPaint(
                RemoteBrush.linearGradient(
                    listOf(Transparent, colors.primary, colors.secondary, colors.tertiary, Transparent),
                ),
            ),
            RemoteOffset(width * 0.15f, height - 3.rdp.toPx()),
            RemoteSize(width * 0.7f, 2.rdp.toPx()),
        )
    }
}

private fun RemoteDrawScope.confetti(colors: BackdropColors) {
    // In the card's padding down the end edge, and along the bottom, clear of the text.
    piece(colors.primary, fromRight = 9f, y = 0.1f, kind = Piece.Strip, angle = 35f)
    piece(colors.tertiary, fromRight = 18f, y = 0.05f, kind = Piece.Dot)
    piece(colors.secondary, fromRight = 6f, y = 0.3f, kind = Piece.Dot)
    piece(colors.tertiary, fromRight = 30f, y = 0.94f, kind = Piece.Strip, angle = -25f)
    piece(colors.primary, fromRight = 48f, y = 0.96f, kind = Piece.Dot)
    piece(colors.secondary, fromLeft = 14f, y = 0.93f, kind = Piece.Strip, angle = -50f)
    piece(colors.tertiary, fromLeft = 28f, y = 0.96f, kind = Piece.Dot)
    piece(colors.primary, fromLeft = 40f, y = 0.92f, kind = Piece.Strip, angle = 15f)
}

private enum class Piece { Dot, Strip }

/** One piece of confetti, placed [fromLeft] or [fromRight] (dp) and at [y] of the height. */
private fun RemoteDrawScope.piece(
    color: RemoteColor,
    fromLeft: Float? = null,
    fromRight: Float? = null,
    y: Float,
    kind: Piece,
    angle: Float = 0f,
) {
    val x: RemoteFloat = if (fromRight != null) width - fromRight.rdp.toPx() else fromLeft!!.rdp.toPx()
    val cy = height * y
    val p = paint(color.copy(alpha = 0.75f.rf))
    when (kind) {
        Piece.Dot -> drawCircle(p, 2.5f.rdp.toPx(), RemoteOffset(x, cy))
        Piece.Strip -> {
            val w = 7.rdp.toPx()
            val h = 3.rdp.toPx()
            rotate(angle.rf, RemoteOffset(x, cy)) {
                drawRect(p, RemoteOffset(x - w / 2f, cy - h / 2f), RemoteSize(w, h))
            }
        }
    }
}

private fun RemoteDrawScope.droidconPanel(colors: BackdropColors) {
    // The rule down the start edge.
    drawRect(paint(colors.primary), RemoteOffset(0f.rf, 0f.rf), RemoteSize(4.rdp.toPx(), height))
    // A dot matrix in the top-right corner, fading towards the text.
    val step = 7.rdp.toPx()
    val r = 1.2f.rdp.toPx()
    for (row in 0 until 4) {
        for (col in 0 until 5) {
            val alpha = 0.45f - 0.08f * (col + row)
            if (alpha <= 0f) continue
            drawCircle(
                paint(colors.primary.copy(alpha = alpha.rf)),
                r,
                RemoteOffset(width - 14.rdp.toPx() - step * col.toFloat(), 12.rdp.toPx() + step * row.toFloat()),
            )
        }
    }
}

private fun RemoteDrawScope.kotlinConfGlow(colors: BackdropColors) {
    glow(colors.primary, 0.5f, RemoteOffset(width, 0f.rf), width * 0.75f)
    glow(colors.secondary, 0.3f, RemoteOffset(0f.rf, height), width * 0.55f)
    glow(colors.tertiary, 0.2f, RemoteOffset(width, height), width * 0.35f)
}

private fun RemoteDrawScope.glow(color: RemoteColor, alpha: Float, center: RemoteOffset, radius: RemoteFloat) {
    drawRect(brushPaint(RemoteBrush.radialGradient(listOf(color.copy(alpha = alpha.rf), Transparent), center, radius)))
}

private fun paint(color: RemoteColor) = RemotePaint { this.color = color }

private fun RemoteDrawScope.brushPaint(brush: RemoteBrush): RemotePaint =
    RemotePaint { with(brush) { applyTo(this@RemotePaint, size) } }
