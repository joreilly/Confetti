package dev.johnoreilly.confetti.test.screenshot

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.quickbird.snapshot.Diffing
import com.quickbird.snapshot.Snapshotting
import com.quickbird.snapshot.fileSnapshotting
import com.quickbird.snapshot.snapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Create screenshot testing rule for robolectric native graphics tests.
 * To use this you should enable native graphics feature from Robolectric
 *
 * @param record Set this to true if you want to record the screenshots. Default value is false.
 */
fun createScreenshotTestRule(record: Boolean = false): ScreenshotTestRule {
    return ScreenshotTestRuleImpl(record = record)
}

interface ScreenshotTestRule: TestRule {
    /**
     * Set configuration properties important for the screenshot testing rule.
     *
     * @param tolerance This is the percentage difference allowed for the test to pass.
     * @param snapshotTransformer
     */
    fun setConfiguration(
        tolerance: Float = 0.1f,
        snapshotTransformer: SnapshotTransformer = SnapshotTransformer.None,
    )

    @ExperimentalCoroutinesApi
    fun takeScreenshot(
        checks: suspend () -> Unit = {},
        content: @Composable () -> Unit
    )
}

class ScreenshotTestRuleImpl(private val record: Boolean = false) : ScreenshotTestRule {

    private val composeTestRule = createComposeRule()
    private val testName: TestName = TestName()

    private val directoryName = this::class.simpleName!!
    private var tolerance: Float = 0.1f
    private var snapshotTransformer: SnapshotTransformer = SnapshotTransformer.None
    override fun setConfiguration(
        tolerance: Float,
        snapshotTransformer: SnapshotTransformer,
    ) {
        this.tolerance = tolerance
        this.snapshotTransformer = snapshotTransformer
    }

    @ExperimentalCoroutinesApi
    override fun takeScreenshot(
        checks: suspend () -> Unit,
        content: @Composable () -> Unit
    ) {
        runTest {
            lateinit var view: View

            composeTestRule.setContent {
                view = LocalView.current
                content()
            }

            composeTestRule.awaitIdle()

            checks()

            val snapshotting = Snapshotting(
                diffing = Diffing.bitmapWithTolerance(
                    tolerance = tolerance,
                    colorDiffing = Diffing.highlightWithRed
                ),
                snapshot = { node: SemanticsNodeInteraction ->
                    val bitmap =
                        Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                            .apply {
                                view.draw(Canvas(this))
                            }
                    snapshotTransformer.transform(node, bitmap)
                }
            ).fileSnapshotting

            // Flip to true to record
            snapshotting.snapshot(
                composeTestRule.onRoot(),
                directoryName = directoryName,
                record = record
            )
        }
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                RuleChain.outerRule(testName)
                    .around(composeTestRule)
                    .apply(base, description)
            }
        }
    }
}
