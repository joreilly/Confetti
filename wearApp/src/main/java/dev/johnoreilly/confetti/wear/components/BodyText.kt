package dev.johnoreilly.confetti.wear.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight

/**
 * Extra horizontal inset for multi-line body text, on top of the ScreenScaffold content padding.
 *
 * Body text is laid out as a rectangle, but on a round screen the chord gets narrower towards
 * the top and bottom, so the corners of a full-width paragraph fall outside the display.
 */
@Composable
fun bodyTextHorizontalPadding(): Dp {
    val configuration = LocalConfiguration.current
    return if (configuration.isScreenRound) {
        (configuration.screenWidthDp * BodyTextExtraHorizontalPaddingFraction).dp
    } else {
        0.dp
    }
}

private const val BodyTextExtraHorizontalPaddingFraction = 0.05f

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
 * Adds [text] to the list as one item per sentence, inset for round screens.
 */
fun TransformingLazyColumnScope.bodyTextItems(
    text: String,
    transformationSpec: TransformationSpec,
    keyPrefix: String,
) {
    val chunks = splitBodyText(text)
    items(count = chunks.size, key = { "$keyPrefix-$it" }) { index ->
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    with(transformationSpec) {
                        applyContainerTransformation(scrollProgress)
                    }
                }
                .transformedHeight(this, transformationSpec)
                .padding(horizontal = bodyTextHorizontalPadding()),
            text = chunks[index],
            textAlign = TextAlign.Start,
        )
    }
}

/**
 * List content padding for a screen whose last item is body text.
 *
 * The default bottom padding leaves the final line in the narrowest part of a round screen, where
 * its corners are cut off. Raise the bottom padding so the last line sits where the display is
 * wide enough for it.
 */
@Composable
fun bodyTextContentPadding(contentPadding: PaddingValues): PaddingValues {
    val configuration = LocalConfiguration.current
    if (!configuration.isScreenRound) return contentPadding
    val layoutDirection = LocalLayoutDirection.current
    val minBottom = (configuration.screenHeightDp * BodyTextMinBottomPaddingFraction).dp
    return PaddingValues(
        start = contentPadding.calculateStartPadding(layoutDirection),
        top = contentPadding.calculateTopPadding(),
        end = contentPadding.calculateEndPadding(layoutDirection),
        bottom = maxOf(contentPadding.calculateBottomPadding(), minBottom),
    )
}

private const val BodyTextMinBottomPaddingFraction = 0.2f
