package dev.johnoreilly.confetti.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ConfettiIcons.Linkedin: ImageVector
    get() {
        if (_Linkedin != null) {
            return _Linkedin!!
        }
        _Linkedin = ImageVector.Builder(
            name = "Linkedin",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 56.693f,
            viewportHeight = 56.693f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(30.071f, 27.101f)
                verticalLineToRelative(-0.077f)
                curveToRelative(-0.016f, 0.026f, -0.033f, 0.052f, -0.05f, 0.077f)
                horizontalLineTo(30.071f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(49.265f, 4.667f)
                horizontalLineTo(7.145f)
                curveToRelative(-2.016f, 0f, -3.651f, 1.596f, -3.651f, 3.563f)
                verticalLineToRelative(42.613f)
                curveToRelative(0f, 1.966f, 1.635f, 3.562f, 3.651f, 3.562f)
                horizontalLineToRelative(42.12f)
                curveToRelative(2.019f, 0f, 3.654f, -1.597f, 3.654f, -3.562f)
                verticalLineTo(8.23f)
                curveTo(52.919f, 6.262f, 51.283f, 4.667f, 49.265f, 4.667f)
                close()
                moveTo(18.475f, 46.304f)
                horizontalLineToRelative(-7.465f)
                verticalLineTo(23.845f)
                horizontalLineToRelative(7.465f)
                verticalLineTo(46.304f)
                close()
                moveTo(14.743f, 20.777f)
                horizontalLineToRelative(-0.05f)
                curveToRelative(-2.504f, 0f, -4.124f, -1.725f, -4.124f, -3.88f)
                curveToRelative(0f, -2.203f, 1.67f, -3.88f, 4.223f, -3.88f)
                curveToRelative(2.554f, 0f, 4.125f, 1.677f, 4.175f, 3.88f)
                curveTo(18.967f, 19.052f, 17.345f, 20.777f, 14.743f, 20.777f)
                close()
                moveTo(45.394f, 46.304f)
                horizontalLineToRelative(-7.465f)
                verticalLineTo(34.286f)
                curveToRelative(0f, -3.018f, -1.08f, -5.078f, -3.781f, -5.078f)
                curveToRelative(-2.062f, 0f, -3.29f, 1.389f, -3.831f, 2.731f)
                curveToRelative(-0.197f, 0.479f, -0.245f, 1.149f, -0.245f, 1.821f)
                verticalLineToRelative(12.543f)
                horizontalLineToRelative(-7.465f)
                curveToRelative(0f, 0f, 0.098f, -20.354f, 0f, -22.459f)
                horizontalLineToRelative(7.465f)
                verticalLineToRelative(3.179f)
                curveToRelative(0.992f, -1.53f, 2.766f, -3.709f, 6.729f, -3.709f)
                curveToRelative(4.911f, 0f, 8.594f, 3.211f, 8.594f, 10.11f)
                verticalLineTo(46.304f)
                close()
            }
        }.build()

        return _Linkedin!!
    }

private var _Linkedin: ImageVector? = null
