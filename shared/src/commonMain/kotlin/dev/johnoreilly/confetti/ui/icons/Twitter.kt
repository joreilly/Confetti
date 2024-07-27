package dev.johnoreilly.confetti.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ConfettiIcons.Twitter: ImageVector
    get() {
        if (_Twitter != null) {
            return _Twitter!!
        }
        _Twitter = ImageVector.Builder(
            name = "Twitter",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                strokeLineJoin = StrokeJoin.Round,
                strokeLineMiter = 1.41421f
            ) {
                moveTo(8.29f, 20.127f)
                curveToRelative(7.548f, 0f, 11.675f, -6.253f, 11.675f, -11.675f)
                curveToRelative(0f, -0.177f, -0.004f, -0.354f, -0.012f, -0.53f)
                curveTo(20.754f, 7.343f, 21.451f, 6.62f, 22f, 5.797f)
                curveTo(21.265f, 6.124f, 20.474f, 6.344f, 19.644f, 6.443f)
                curveTo(20.491f, 5.935f, 21.141f, 5.131f, 21.448f, 4.173f)
                curveTo(20.655f, 4.643f, 19.777f, 4.985f, 18.842f, 5.169f)
                curveTo(18.094f, 4.372f, 17.028f, 3.873f, 15.847f, 3.873f)
                curveToRelative(-2.266f, 0f, -4.104f, 1.838f, -4.104f, 4.103f)
                curveToRelative(0f, 0.322f, 0.036f, 0.635f, 0.107f, 0.935f)
                curveTo(8.439f, 8.74f, 5.415f, 7.107f, 3.392f, 4.624f)
                curveTo(3.039f, 5.231f, 2.836f, 5.935f, 2.836f, 6.687f)
                curveToRelative(0f, 1.423f, 0.725f, 2.68f, 1.826f, 3.415f)
                curveTo(3.989f, 10.081f, 3.357f, 9.897f, 2.804f, 9.589f)
                curveTo(2.803f, 9.606f, 2.803f, 9.623f, 2.803f, 9.641f)
                curveToRelative(0f, 1.988f, 1.415f, 3.647f, 3.292f, 4.023f)
                curveToRelative(-0.345f, 0.094f, -0.707f, 0.144f, -1.082f, 0.144f)
                curveToRelative(-0.264f, 0f, -0.521f, -0.026f, -0.771f, -0.074f)
                curveToRelative(0.522f, 1.631f, 2.037f, 2.817f, 3.833f, 2.85f)
                curveToRelative(-1.404f, 1.101f, -3.173f, 1.757f, -5.096f, 1.757f)
                curveToRelative(-0.331f, 0f, -0.658f, -0.019f, -0.979f, -0.057f)
                curveToRelative(1.816f, 1.164f, 3.972f, 1.843f, 6.29f, 1.843f)
            }
        }.build()

        return _Twitter!!
    }

private var _Twitter: ImageVector? = null
