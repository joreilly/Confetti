package dev.johnoreilly.confetti.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ConfettiIcons.Slack: ImageVector
    get() {
        if (_Slack != null) {
            return _Slack!!
        }
        _Slack = ImageVector.Builder(
            name = "Slack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(5.042f, 15.165f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, -2.52f, 2.523f)
                arcTo(2.52f, 2.52f, 0.0f, false, true, 0.0f, 15.165f)
                arcToRelative(2.527f, 2.527f, 0.0f, false, true, 2.522f, -2.52f)
                horizontalLineToRelative(2.52f)
                verticalLineToRelative(2.52f)
                close()
                moveTo(6.313f, 15.165f)
                arcToRelative(2.527f, 2.527f, 0.0f, false, true, 2.521f, -2.52f)
                arcToRelative(2.521f, 2.521f, 0.0f, false, true, 2.521f, 2.52f)
                verticalLineToRelative(6.313f)
                arcTo(2.528f, 2.528f, 0.0f, false, true, 8.834f, 24.0f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, -2.521f, -2.522f)
                verticalLineToRelative(-6.313f)
                close()
                moveTo(8.834f, 5.042f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, -2.521f, -2.52f)
                arcTo(2.52f, 2.52f, 0.0f, false, true, 8.834f, 0.0f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, 2.521f, 2.522f)
                verticalLineToRelative(2.52f)
                horizontalLineTo(8.834f)
                close()
                moveTo(8.834f, 6.313f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, 2.521f, 2.521f)
                arcToRelative(2.52f, 2.52f, 0.0f, false, true, -2.521f, 2.52f)
                horizontalLineTo(2.522f)
                arcTo(2.528f, 2.528f, 0.0f, false, true, 0.0f, 8.834f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, 2.522f, -2.521f)
                horizontalLineToRelative(6.312f)
                close()
                moveTo(18.956f, 8.834f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, 2.522f, -2.521f)
                arcTo(2.52f, 2.52f, 0.0f, false, true, 24.0f, 8.834f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, -2.522f, 2.521f)
                horizontalLineToRelative(-2.522f)
                verticalLineTo(8.834f)
                close()
                moveTo(17.688f, 8.834f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, -2.523f, 2.521f)
                arcToRelative(2.527f, 2.527f, 0.0f, false, true, -2.52f, -2.521f)
                verticalLineTo(2.522f)
                arcTo(2.527f, 2.527f, 0.0f, false, true, 15.165f, 0.0f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, 2.523f, 2.522f)
                verticalLineToRelative(6.312f)
                close()
                moveTo(15.165f, 18.956f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, 2.523f, 2.522f)
                arcTo(2.52f, 2.52f, 0.0f, false, true, 15.165f, 24.0f)
                arcToRelative(2.527f, 2.527f, 0.0f, false, true, -2.52f, -2.522f)
                verticalLineToRelative(-2.522f)
                horizontalLineToRelative(2.52f)
                close()
                moveTo(15.165f, 17.688f)
                arcToRelative(2.527f, 2.527f, 0.0f, false, true, -2.52f, -2.523f)
                arcToRelative(2.52f, 2.52f, 0.0f, false, true, 2.52f, -2.52f)
                horizontalLineToRelative(6.313f)
                arcTo(2.527f, 2.527f, 0.0f, false, true, 24.0f, 15.165f)
                arcToRelative(2.528f, 2.528f, 0.0f, false, true, -2.522f, 2.523f)
                horizontalLineToRelative(-6.313f)
                close()
            }
        }.build()

        return _Slack!!
    }

private var _Slack: ImageVector? = null
