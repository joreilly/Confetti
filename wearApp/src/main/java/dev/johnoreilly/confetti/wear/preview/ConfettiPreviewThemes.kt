package dev.johnoreilly.confetti.wear.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.wear.compose.material.Colors
import com.google.android.horologist.compose.tools.ThemeValues
import dev.johnoreilly.confetti.wear.settings.toMaterialThemeColors

class ConfettiPreviewThemes : PreviewParameterProvider<ThemeValues> {
    override val values: Sequence<ThemeValues>
        get() = listOf(
            ThemeValues("Material", 0, Colors()),
            ThemeValues("MobileDefault", 1, TestFixtures.MobileTheme.toMaterialThemeColors()),
            ThemeValues("MobileAndroid", 2, TestFixtures.AndroidTheme.toMaterialThemeColors())
        ).asSequence()
}