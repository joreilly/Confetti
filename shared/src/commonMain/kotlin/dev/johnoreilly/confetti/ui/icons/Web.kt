package dev.johnoreilly.confetti.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ConfettiIcons.Web: ImageVector
    get() {
        if (_Web != null) {
            return _Web!!
        }
        _Web = ImageVector.Builder(
            name = "Web",
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
                moveTo(11.99f, 2f)
                curveTo(6.47f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.47f, 22f, 11.99f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 11.99f, 2f)
                close()
                moveToRelative(6.93f, 6f)
                lineToRelative(-2.95f, 0f)
                curveTo(15.65f, 6.75f, 15.19f, 5.55f, 14.59f, 4.44f)
                curveTo(16.43f, 5.07f, 17.96f, 6.35f, 18.92f, 8f)
                close()
                moveTo(12f, 4.04f)
                curveToRelative(0.83f, 1.2f, 1.48f, 2.53f, 1.91f, 3.96f)
                lineTo(10.09f, 8f)
                curveTo(10.52f, 6.57f, 11.17f, 5.24f, 12f, 4.04f)
                close()
                moveTo(4.26f, 14f)
                curveTo(4.1f, 13.36f, 4f, 12.69f, 4f, 12f)
                curveTo(4f, 11.31f, 4.1f, 10.64f, 4.26f, 10f)
                lineToRelative(3.38f, 0f)
                curveToRelative(-0.08f, 0.66f, -0.14f, 1.32f, -0.14f, 2f)
                curveToRelative(0f, 0.68f, 0.06f, 1.34f, 0.14f, 2f)
                lineToRelative(-3.38f, 0f)
                close()
                moveToRelative(0.82f, 2f)
                lineToRelative(2.95f, 0f)
                curveToRelative(0.32f, 1.25f, 0.78f, 2.45f, 1.38f, 3.56f)
                curveTo(7.57f, 18.93f, 6.04f, 17.66f, 5.08f, 16f)
                lineToRelative(0f, 0f)
                close()
                moveTo(8.03f, 8f)
                lineTo(5.08f, 8f)
                curveTo(6.04f, 6.34f, 7.57f, 5.07f, 9.41f, 4.44f)
                curveTo(8.81f, 5.55f, 8.35f, 6.75f, 8.03f, 8f)
                lineToRelative(0f, 0f)
                close()
                moveTo(12f, 19.96f)
                curveTo(11.17f, 18.76f, 10.52f, 17.43f, 10.09f, 16f)
                lineToRelative(3.82f, 0f)
                curveTo(13.48f, 17.43f, 12.83f, 18.76f, 12f, 19.96f)
                close()
                moveTo(14.34f, 14f)
                lineTo(9.66f, 14f)
                curveTo(9.57f, 13.34f, 9.5f, 12.68f, 9.5f, 12f)
                curveToRelative(0f, -0.68f, 0.07f, -1.35f, 0.16f, -2f)
                lineToRelative(4.68f, 0f)
                curveToRelative(0.09f, 0.65f, 0.16f, 1.32f, 0.16f, 2f)
                curveToRelative(0f, 0.68f, -0.07f, 1.34f, -0.16f, 2f)
                close()
                moveToRelative(0.25f, 5.56f)
                curveToRelative(0.6f, -1.11f, 1.06f, -2.31f, 1.38f, -3.56f)
                lineToRelative(2.95f, 0f)
                curveToRelative(-0.96f, 1.65f, -2.49f, 2.93f, -4.33f, 3.56f)
                lineToRelative(0f, 0f)
                close()
                moveTo(16.36f, 14f)
                curveToRelative(0.08f, -0.66f, 0.14f, -1.32f, 0.14f, -2f)
                curveToRelative(0f, -0.68f, -0.06f, -1.34f, -0.14f, -2f)
                lineToRelative(3.38f, 0f)
                curveToRelative(0.16f, 0.64f, 0.26f, 1.31f, 0.26f, 2f)
                curveToRelative(0f, 0.69f, -0.1f, 1.36f, -0.26f, 2f)
                lineToRelative(-3.38f, 0f)
                close()
            }
        }.build()

        return _Web!!
    }

private var _Web: ImageVector? = null
