/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.johnoreilly.confetti.screenshot.a11y

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import dev.johnoreilly.confetti.screenshot.SnapshotTransformer

class A11ySnapshotTransformer : SnapshotTransformer {
    lateinit var elements: List<AccessibilityState.Element>

    val colors = listOf(
        Color.Blue,
        Color.Cyan,
        Color.Green,
        Color.Gray,
        Color.Magenta,
        Color.LightGray,
        Color.Yellow,
    )

    private fun colorForIndex(i: Int): Color {
        return colors[i % colors.size]
    }

    override fun transform(node: SemanticsNodeInteraction, bitmap: Bitmap): Bitmap {
        val semanticsNode = node.fetchSemanticsNode()

        elements = buildList {
            processAccessibleChildren(semanticsNode) {
                add(it)
            }
        }

        return Bitmap.createBitmap(bitmap.width * 2, bitmap.height, bitmap.config).apply {
            val canvas = Canvas(this)

            drawImageWithOverlays(canvas, bitmap)

            drawLegend(canvas, elements)
        }
    }

    fun drawImageWithOverlays(
        canvas: Canvas,
        originalBitmap: Bitmap
    ) {
        canvas.drawBitmap(
            originalBitmap,
            0f,
            0f,
            Paint().apply { alpha = 180 })

        elements.forEachIndexed { i, it ->
            val bounds = it.touchBounds ?: it.displayBounds

            val paint = Paint().apply {
                color = colorForIndex(i).copy(alpha = 0.25f).toArgb()
                strokeWidth = 3f
            }
            canvas.drawRect(bounds, paint)
        }
    }

    fun drawLegend(
        canvas: Canvas,
        elements: List<AccessibilityState.Element>
    ) {
        val height = canvas.height
        val width = canvas.width
        val leftEdge = width / 2

        canvas.drawRect(
            Rect(leftEdge, 0, width, height),
            Paint().apply { color = Color.White.toArgb() })

//        font = font.deriveFont(20f)
//        stroke = BasicStroke(3f)

        var index = 1
        val paint = Paint().apply {
            alpha = 80
        }
        val textPaint = Paint().apply {
            strokeWidth = 3f
            typeface = Typeface.SANS_SERIF
            textSize = 20f
        }

        fun drawItem(s: String) {
            canvas.drawText(s, 50f + leftEdge, 28f * index++, textPaint)
        }

        elements.forEachIndexed { i, it ->
            paint.color = android.graphics.Color.BLACK

            val start = index
            if (it.role != null || it.disabled || it.heading) {
                val role = if (it.role != null) "Role " + it.role + " " else ""
                val heading = if (it.heading) "Heading " else ""
                val disabled = if (it.disabled) "Disabled" else ""
                drawItem(role + heading + disabled)
            }
            if (it.contentDescription != null) {
                drawItem("Content Description \"${it.contentDescription.joinToString(", ")}\"")
            } else if (it.text != null) {
                drawItem("Text \"${it.text.joinToString(", ")}\"")
            }
            if (it.stateDescription != null) {
                drawItem("State Description \"${it.stateDescription}\"")
            }
            if (it.onClickLabel != null) {
                drawItem("On Click \"${it.onClickLabel}\"")
            }
            if (it.progress != null) {
                drawItem("Progress \"${it.progress}\"")
            }
            if (it.customActions != null) {
                for (action in it.customActions) {
                    drawItem("Custom Action \"${action.label}\"")
                }
            }
            val end = index

            paint.color = colorForIndex(i).toArgb()
            paint.alpha = 50
            canvas.drawRect(
                Rect(
                    10 + leftEdge,
                    start * 28 - 21,
                    width - 20,
                    end * 28 - 21,
                ),
                paint
            )

            index++
        }
    }

    private fun processAccessibleChildren(
        p0: SemanticsNode,
        fn: (AccessibilityState.Element) -> Unit
    ) {
        val contentDescription = p0.config.getOrNull(SemanticsProperties.ContentDescription)
        val stateDescription = p0.config.getOrNull(SemanticsProperties.StateDescription)
        val onClickLabel = p0.config.getOrNull(SemanticsActions.OnClick)?.label
        val role = p0.config.getOrNull(SemanticsProperties.Role)?.toString()
        val disabled = p0.config.getOrNull(SemanticsProperties.Disabled) != null
        val heading = p0.config.getOrNull(SemanticsProperties.Heading) != null
        val customActions = p0.config.getOrNull(SemanticsActions.CustomActions)
        val text = p0.config.getOrNull(SemanticsProperties.Text)
        val progress = p0.config.getOrNull(SemanticsProperties.ProgressBarRangeInfo)
        val hasProgressAction = p0.config.getOrNull(SemanticsActions.SetProgress) != null

        if (contentDescription != null || stateDescription != null || onClickLabel != null || role != null || progress != null || text != null) {
            val position = Rect(
                p0.boundsInRoot.left.toInt(),
                p0.boundsInRoot.top.toInt(),
                p0.boundsInRoot.right.toInt(),
                p0.boundsInRoot.bottom.toInt()
            )
            val touchBounds = Rect(
                p0.touchBoundsInRoot.left.toInt(),
                p0.touchBoundsInRoot.top.toInt(),
                p0.touchBoundsInRoot.right.toInt(),
                p0.touchBoundsInRoot.bottom.toInt()
            )
            fn(
                AccessibilityState.Element(
                    position,
                    if (touchBounds != position) touchBounds else null,
                    text?.map { it.toString() },
                    contentDescription,
                    stateDescription,
                    onClickLabel,
                    role,
                    disabled,
                    heading,
                    customActions?.map { AccessibilityState.CustomAction(label = it.label) },
                    progress?.let {
                        AccessibilityState.Progress(
                            it.current,
                            it.range,
                            it.steps,
                            hasProgressAction
                        )
                    }
                )
            )
        }

        for (it in p0.children) {
            processAccessibleChildren(it, fn)
        }
    }
}