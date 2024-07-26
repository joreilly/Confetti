package dev.johnoreilly.confetti.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ConfettiIcons.Facebook: ImageVector
    get() {
        if (_Facebook != null) {
            return _Facebook!!
        }
        _Facebook = ImageVector.Builder(
            name = "Facebook",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(449.45f, 0f)
                curveToRelative(34.53f, 0f, 62.55f, 28.03f, 62.55f, 62.55f)
                lineToRelative(0f, 386.89f)
                curveToRelative(0f, 34.52f, -28.03f, 62.55f, -62.55f, 62.55f)
                lineToRelative(-106.47f, 0f)
                lineToRelative(0f, -192.91f)
                lineToRelative(66.6f, 0f)
                lineToRelative(12.67f, -82.62f)
                lineToRelative(-79.27f, 0f)
                lineToRelative(0f, -53.62f)
                curveToRelative(0f, -22.6f, 11.07f, -44.64f, 46.58f, -44.64f)
                lineToRelative(36.04f, 0f)
                lineToRelative(0f, -70.34f)
                curveToRelative(0f, 0f, -32.71f, -5.58f, -63.98f, -5.58f)
                curveToRelative(-65.29f, 0f, -107.96f, 39.57f, -107.96f, 111.2f)
                lineToRelative(0f, 62.97f)
                lineToRelative(-72.57f, 0f)
                lineToRelative(0f, 82.62f)
                lineToRelative(72.57f, 0f)
                lineToRelative(0f, 192.91f)
                lineToRelative(-191.1f, 0f)
                curveToRelative(-34.52f, 0f, -62.55f, -28.03f, -62.55f, -62.55f)
                lineToRelative(0f, -386.89f)
                curveToRelative(0f, -34.52f, 28.03f, -62.55f, 62.55f, -62.55f)
                lineToRelative(386.89f, 0f)
                close()
            }
        }.build()

        return _Facebook!!
    }

private var _Facebook: ImageVector? = null
