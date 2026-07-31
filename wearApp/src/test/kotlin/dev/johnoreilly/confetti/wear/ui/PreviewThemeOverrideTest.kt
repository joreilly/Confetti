package dev.johnoreilly.confetti.wear.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.wear.compose.material3.MaterialTheme
import dev.johnoreilly.confetti.ui.PreviewThemeOverrideInstalled
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards the preview theme override switch — see `PreviewThemeOverride` in `:shared`.
 *
 * The design catalog's previews install Confetti's theme in their own body, which is what makes the
 * Android Studio preview pane and a plain `compose-preview` render show the real app theme. The
 * preview server applies an alternative theme by *wrapping* the preview, so without the opt-out that
 * body theme composes inside the wrapper and shadows it — and every entry in the viewer's Theme
 * select renders identical pixels. These tests pin the three behaviours that stops.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    // A bare Application: the real ConfettiApplication configures notification bridging, which
    // throws off a wearable device, and the Koin test app can't be started once per test in a
    // class. Nothing here needs either — these tests only read a resolved MaterialTheme.
    application = Application::class,
    sdk = [34],
)
class PreviewThemeOverrideTest {

    @get:Rule
    val composeRule = createComposeRule()

    /**
     * Composes each of [cases] once and returns the `MaterialTheme.colorScheme.primary` each one
     * resolves to. They all go through a single `setContent` — the rule allows only one per test —
     * as sibling subtrees, which is fine because the probe emits no UI, it just reads the theme.
     */
    private fun primariesOf(
        vararg cases: @Composable (@Composable () -> Unit) -> Unit,
    ): List<Color> {
        val captured = arrayOfNulls<Color>(cases.size)
        composeRule.setContent {
            cases.forEachIndexed { i, case ->
                case { captured[i] = MaterialTheme.colorScheme.primary }
            }
        }
        composeRule.waitForIdle()
        return captured.map { requireNotNull(it) { "a case never composed" } }
    }

    /** A preview body that installs the app theme itself, as every catalog preview does. */
    @Composable
    private fun BodyTheme(probe: @Composable () -> Unit) {
        ConfettiThemeFixed { probe() }
    }

    /** What a `@WearThemeCatalog` provider wraps a preview in. */
    @Composable
    private fun Override(conferenceId: String?, content: @Composable () -> Unit) {
        ConfettiConferenceTheme(conferenceId = conferenceId) {
            PreviewThemeOverrideInstalled(content)
        }
    }

    @Test
    fun `body theme stands down under an override`() {
        val (overrideOnly, overrideThenBody) = primariesOf(
            // The override alone — the look the viewer asked for.
            { probe -> Override("kotlinconf2025") { probe() } },
            // The same override, with a preview body that also installs the app theme. The body
            // must yield, so this has to land on exactly the same colour.
            { probe -> Override("kotlinconf2025") { BodyTheme(probe) } },
        )

        assertEquals(overrideOnly, overrideThenBody)
    }

    @Test
    fun `body theme still applies with no override`() {
        val (overrideOnly, bodyOnly) = primariesOf(
            { probe -> Override("kotlinconf2025") { probe() } },
            { probe -> BodyTheme(probe) },
        )

        // Nothing claimed the theme, so the body's own theme is what renders — the Android Studio /
        // CLI / production path, which must NOT look like the override.
        assertNotEquals(overrideOnly, bodyOnly)
    }

    @Test
    fun `deliberate nested theming survives an override`() {
        val (nested, droidconAlone) = primariesOf(
            // Standing down consumes the flag, so only the OUTERMOST app theme yields. A theme
            // nested deeper on purpose — ConferencesView tinting each row by that conference's seed
            // colour — still applies rather than being flattened into the override.
            { probe ->
                Override("kotlinconf2025") {
                    ConfettiThemeFixed {
                        ConfettiConferenceTheme(conferenceId = "droidconlondon2025") { probe() }
                    }
                }
            },
            { probe -> Override("droidconlondon2025") { probe() } },
        )

        assertEquals(droidconAlone, nested)
    }
}
