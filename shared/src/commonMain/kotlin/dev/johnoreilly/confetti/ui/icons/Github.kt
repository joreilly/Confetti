package dev.johnoreilly.confetti.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ConfettiIcons.Github: ImageVector
    get() {
        if (_Github != null) {
            return _Github!!
        }
        _Github = ImageVector.Builder(
            name = "Github",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF181717)),
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(49.95f, 1.48f)
                arcTo(50.03f, 50.03f, 89.98f, false, false, 34.3f, 98.79f)
                curveToRelative(2.46f, 0.61f, 3.07f, -0.92f, 3.07f, -2.15f)
                lineToRelative(0f, -8.59f)
                curveTo(23.55f, 91.11f, 20.79f, 80.98f, 20.79f, 80.98f)
                curveToRelative(-2.46f, -5.83f, -5.83f, -7.37f, -5.83f, -7.37f)
                curveToRelative(-4.3f, -3.07f, 0.61f, -3.07f, 0.61f, -3.07f)
                curveToRelative(4.91f, 0.31f, 7.67f, 5.22f, 7.67f, 5.22f)
                curveTo(27.55f, 84.05f, 34.6f, 81.6f, 37.67f, 80.37f)
                curveToRelative(0.31f, -3.07f, 1.84f, -5.52f, 3.07f, -6.75f)
                curveToRelative(-11.05f, -1.23f, -22.71f, -5.52f, -22.71f, -24.56f)
                curveToRelative(0f, -5.52f, 1.84f, -10.13f, 5.22f, -13.51f)
                curveToRelative(-0.61f, -1.23f, -2.15f, -6.45f, 0.31f, -13.2f)
                curveToRelative(0f, 0f, 4.3f, -1.53f, 13.81f, 4.91f)
                arcToRelative(47.88f, 47.89f, 90.02f, false, true, 25.17f, 0f)
                curveToRelative(9.21f, -6.14f, 13.81f, -4.91f, 13.81f, -4.91f)
                curveToRelative(2.46f, 6.75f, 0.92f, 11.97f, 0.31f, 13.2f)
                curveToRelative(3.07f, 3.38f, 5.22f, 7.98f, 5.22f, 13.2f)
                curveToRelative(0f, 19.34f, -11.66f, 23.63f, -22.71f, 24.56f)
                curveToRelative(1.53f, 1.84f, 3.07f, 4.91f, 3.07f, 9.21f)
                lineToRelative(0f, 14.12f)
                curveToRelative(0f, 1.23f, 0.92f, 2.76f, 3.68f, 2.15f)
                arcTo(50.03f, 50.03f, 89.98f, false, false, 49.95f, 1.48f)
            }
        }.build()

        return _Github!!
    }

private var _Github: ImageVector? = null
