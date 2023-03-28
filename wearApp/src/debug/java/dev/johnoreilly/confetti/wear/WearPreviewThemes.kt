package dev.johnoreilly.confetti.wear

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.wear.compose.material.Colors
import com.google.android.horologist.compose.tools.ThemeValues
import dev.johnoreilly.confetti.wear.settings.toMaterialThemeColors

class ConfettiPreviewThemes : PreviewParameterProvider<ThemeValues> {
    override val values: Sequence<ThemeValues>
        get() = listOf(
            ThemeValues("Material", 0, Colors()),
            ThemeValues("Mobile", 2, TestFixtures.MobileTheme.toMaterialThemeColors())
        ).asSequence()
}