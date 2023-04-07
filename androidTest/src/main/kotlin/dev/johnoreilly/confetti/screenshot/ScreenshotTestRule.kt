package dev.johnoreilly.confetti.screenshot

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.quickbird.snapshot.Diffing
import com.quickbird.snapshot.FileSnapshotting
import com.quickbird.snapshot.Snapshotting
import com.quickbird.snapshot.fileSnapshotting
import com.quickbird.snapshot.snapshot
import dev.johnoreilly.confetti.screenshot.a11y.A11ySnapshotTransformer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

interface ScreenshotTestRule : TestRule {
    /**
     * Set configuration properties important for the screenshot testing rule.
     *
     * @param snapshotTransformer
     */
    fun setConfiguration(
        snapshotTransformer: SnapshotTransformer = SnapshotTransformer.None,
    )

    @ExperimentalCoroutinesApi
    fun takeScreenshot(
        checks: suspend (rule: ComposeContentTestRule) -> Unit = {},
        content: @Composable () -> Unit
    )
}

abstract class RNGScreenshotTestRule(
    private val record: Boolean = false,
    private val tolerance: Float,
    a11y: Boolean,
    private val directoryName: String,
) : ScreenshotTestRule {

    private val composeTestRule = createComposeRule()
    private val testName: TestName = TestName()

    private var snapshotTransformer: SnapshotTransformer = if (a11y) {
        A11ySnapshotTransformer()
    } else {
        SnapshotTransformer.None
    }

    override fun setConfiguration(
        snapshotTransformer: SnapshotTransformer,
    ) {
        this.snapshotTransformer = snapshotTransformer
    }


    @ExperimentalCoroutinesApi
    override fun takeScreenshot(
        checks: suspend (rule: ComposeContentTestRule) -> Unit,
        content: @Composable () -> Unit
    ) {
        runTest {
            lateinit var view: View

            composeTestRule.setContent {
                view = LocalView.current
                content()
            }

            composeTestRule.awaitIdle()

            checks(composeTestRule)
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

            snapshotting.saveOrVerifySnapshot(
                value = composeTestRule.onRoot(),
            )
        }
    }

    private suspend fun FileSnapshotting<SemanticsNodeInteraction, Bitmap>.saveOrVerifySnapshot(
        value: SemanticsNodeInteraction,
    ) {
        val methodName = testName.methodName.replace("\\W+".toRegex(), "_")
        snapshot(
            value,
            directoryName = directoryName,
            fileName = methodName,
            record = record,
            diffFileName = methodName + "_diff",
        )
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                RuleChain.outerRule(testName)
                    .around(composeTestRule)
                    .apply(base, description)
                    .evaluate()
            }
        }
    }
}
