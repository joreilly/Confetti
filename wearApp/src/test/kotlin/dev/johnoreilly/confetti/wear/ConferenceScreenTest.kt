@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalHorologistComposeLayoutApi::class)
@file:Suppress("UnstableApiUsage")

package dev.johnoreilly.confetti.wear

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.wear.conferences.ConferencesUiState
import dev.johnoreilly.confetti.wear.conferences.ConferencesView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.GraphicsMode.Mode.NATIVE
import org.robolectric.shadows.ShadowDisplay
import org.robolectric.shadows.ShadowDisplayManager
import org.robolectric.shadows.ShadowView
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(application = KoinTestApp::class, sdk = [30])
@GraphicsMode(NATIVE)
class ConferenceScreenTest : KoinTest {
    private lateinit var view: View

    @get:Rule
    val rule = createComposeRule()

    @Before
    fun setUp() {
        val display = ShadowDisplay.getDefaultDisplay()
        ShadowDisplayManager.changeDisplay(display.displayId, "+round")
        shadowOf(display).apply {
            setXdpi(320f)
            setYdpi(320f)
            setHeight(454)
            setWidth(454)
        }
    }

    @Test
    fun conferencesScreen() = runTest {
        rule.setContent {
            view = LocalView.current
            ConferencesView(
                uiState = ConferencesUiState.Success(
                    listOf()
                ),
                navigateToConference = {},
                columnState = ScalingLazyColumnDefaults.belowTimeText().create()
            )
        }

        rule.awaitIdle()

        takeScreenshot()
    }

    private fun takeScreenshot() {
        assertTrue(ShadowView.useRealGraphics())

        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
//        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        bitmap.compress(CompressFormat.PNG, 100, FileOutputStream("test.png"))
    }
}