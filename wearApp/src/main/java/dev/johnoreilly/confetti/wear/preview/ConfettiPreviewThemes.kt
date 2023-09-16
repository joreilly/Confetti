package dev.johnoreilly.confetti.wear.preview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.google.android.horologist.compose.tools.ThemeColors
import com.google.android.horologist.compose.tools.ThemeValues
import dev.johnoreilly.confetti.wear.proto.Theme

class ConfettiPreviewThemes : PreviewParameterProvider<ThemeValues> {
    override val values: Sequence<ThemeValues>
        get() = listOf(
            ThemeValues("Material", 0, ThemeColors()),
            ThemeValues("MobileDefault", 1, TestFixtures.MobileTheme.toThemeColors()),
            ThemeValues("MobileAndroid", 2, TestFixtures.AndroidTheme.toThemeColors())
        ).asSequence()
}

fun Theme.toThemeColors(): ThemeColors {
    return ThemeColors(
        primary = Color(primary),
        primaryVariant = Color(primaryVariant),
        secondary = Color(secondary),
        secondaryVariant = Color(secondaryVariant),
        surface = Color(surface),
        error = Color(error),
        onPrimary = Color(onPrimary),
        onSecondary = Color(onSecondary),
        onBackground = Color(onBackground),
        onSurface = Color(onSurface),
        onSurfaceVariant = Color(onSurfaceVariant),
        onError = Color(onError),
    )
}