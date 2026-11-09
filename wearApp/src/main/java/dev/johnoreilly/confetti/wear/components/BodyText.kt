package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextDefaults
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight

/**
 * Modifier for a plain Text item in a TransformingLazyColumn.
 *
 * - Applies the container transformation so the text scales/fades at the screen edges.
 * - Uses the Wear M3 text list padding, so a list that starts or ends with text gets enough
 *   top/bottom content padding to keep the first/last line inside a round screen.
 * - Adds a round-screen horizontal inset so the corners of multi-line text stay on screen.
 */
@Composable
fun TransformingLazyColumnItemScope.bodyTextModifier(
    transformationSpec: TransformationSpec,
): Modifier = Modifier
    .fillMaxWidth()
    .graphicsLayer {
        with(transformationSpec) {
            applyContainerTransformation(scrollProgress)
        }
    }
    .transformedHeight(this, transformationSpec)
    .minimumVerticalContentPadding(
        TextDefaults.minimumTopListContentPadding,
        TextDefaults.minimumBottomListContentPadding,
    )
    .padding(horizontal = bodyTextHorizontalInset())

/**
 * Extra horizontal inset for multi-line text, on top of the ScreenScaffold content padding.
 *
 * The scaffold padding is enough for pill-shaped components, but a rectangular block of text
 * still has its corners cut off by a round screen. This matches the Wear M3
 * `PaddingDefaults.horizontalContentPaddingPercentage` (internal in Wear Compose M3 1.7).
 */
@Composable
private fun bodyTextHorizontalInset(): Dp {
    val configuration = LocalConfiguration.current
    return if (configuration.isScreenRound) {
        (configuration.screenWidthDp * HorizontalContentPaddingPercentage / 100f).dp
    } else {
        0.dp
    }
}

private const val HorizontalContentPaddingPercentage = 5.2f

/**
 * Splits long text into short, sentence-sized chunks.
 *
 * A single screen-tall Text item can't be scaled in by the TransformingLazyColumn at the top and
 * bottom edges of a round screen, so its first and last lines are cut off by the bezel. Short
 * items are transformed individually as they approach the edges, keeping them on screen.
 */
internal fun splitBodyText(text: String): List<String> =
    text.split("\n+".toRegex())
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .flatMap { paragraph ->
            paragraph.split("(?<=[.!?])\\s+(?=\\S)".toRegex()).map { it.trim() }
        }
        .filter { it.isNotEmpty() }

/**
 * Adds [text] to the list as one item per sentence.
 */
fun TransformingLazyColumnScope.bodyTextItems(
    text: String,
    transformationSpec: TransformationSpec,
    keyPrefix: String,
) {
    val chunks = splitBodyText(text)
    items(count = chunks.size, key = { "$keyPrefix-$it" }) { index ->
        Text(
            modifier = bodyTextModifier(transformationSpec),
            text = chunks[index],
        )
    }
}
