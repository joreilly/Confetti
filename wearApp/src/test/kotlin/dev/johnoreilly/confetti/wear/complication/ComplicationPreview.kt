/*
 * Copyright 2023 The Android Open Source Project
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
@file:Suppress("DEPRECATION")
@file:OptIn(ExperimentalHorologistApi::class)

package dev.johnoreilly.confetti.wear.complication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Icon
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.complication.ComplicationTemplate

/**
 * Preview a Complication by providing the various representation of the complications
 * with the androidx default renderer.
 */
@SuppressLint("NewApi", "ResourceType")
@Composable
fun <D> ComplicationRendererPreview(
    complicationRenderer: ComplicationTemplate<D>,
    data: D = complicationRenderer.previewData(),
    foregroundColor: Color = Color(0xffeea19a),
    backgroundColor: Color = Color.Black
) {
    val context = LocalContext.current
    val types = complicationRenderer.supportedTypes()
    val localDensity = LocalDensity.current

    val rowHeight = with(localDensity) {
        50.dp.toPx()
    }.toInt()

    val wideWidth = with(localDensity) {
        200.dp.toPx()
    }.toInt()

    val gap = with(localDensity) {
        5.dp.toPx()
    }.toInt()

    val hasShortText = ComplicationType.SHORT_TEXT in types
    val count = types.size + if (hasShortText) 1 else 0

    val paint = android.graphics.Paint().apply {
        color = Color.White.toArgb()
        textSize = 10f
    }

    Canvas(
        modifier = Modifier
            .size(200.dp, 105.dp * count)
            .background(Color.DarkGray)
    ) {
        drawIntoCanvas { canvas ->
            types.forEachIndexed { index, complicationType ->
                val drawable =
                    complicationDrawable(
                        complicationRenderer = complicationRenderer,
                        type = complicationType,
                        data = data,
                        context = context,
                        foregroundColor = foregroundColor,
                        backgroundColor = backgroundColor
                    )
                val rowStart = (index * (rowHeight + gap))
                if (complicationType == ComplicationType.LONG_TEXT) {
                    drawable.setBounds(0, rowStart, wideWidth, rowStart + rowHeight)
                } else {
                    drawable.setBounds(0, rowStart, rowHeight, rowStart + rowHeight)
                }
                drawable.draw(canvas.nativeCanvas)

                canvas.nativeCanvas.drawText(
                    complicationType.name,
                    0.toFloat(),
                    (rowStart + rowHeight).toFloat(),
                    paint
                )
            }
            if (hasShortText) {
                val drawable =
                    complicationDrawable(
                        complicationRenderer = complicationRenderer,
                        type = ComplicationType.SHORT_TEXT,
                        data = data,
                        context = context,
                        foregroundColor = foregroundColor,
                        backgroundColor = backgroundColor
                    )
                val rowStart = (types.size * (rowHeight + gap))
                drawable.setBounds(0, rowStart, (rowHeight * 2) + 1, rowStart + rowHeight)

                drawable.draw(canvas.nativeCanvas)

                canvas.nativeCanvas.drawText(
                    ComplicationType.SHORT_TEXT.name + " wide",
                    0.toFloat(),
                    (rowStart + rowHeight).toFloat(),
                    paint
                )
            }
        }
    }
}

private fun <D> complicationDrawable(
    complicationRenderer: ComplicationTemplate<D>,
    type: ComplicationType,
    data: D,
    context: Context,
    foregroundColor: Color,
    backgroundColor: Color
): ComplicationDrawable {
    val complicationData = complicationRenderer.render(type, data)

    fixImages(context, complicationData)

    val drawable = ComplicationDrawable(context)
    drawable.setContext(context)
    drawable.setComplicationData(complicationData, false)
    drawable.activeStyle.textColor = foregroundColor.toArgb()
    drawable.activeStyle.backgroundColor = backgroundColor.toArgb()
    drawable.activeStyle.iconColor = foregroundColor.toArgb()
    drawable.activeStyle.setTextTypeface(Typeface.DEFAULT)
    drawable.noDataText = "No Data"
    return drawable
}

fun fixImages(context: Context, complicationData: ComplicationData) {
    if (complicationData is ShortTextComplicationData) {
        fixImages(context, complicationData.monochromaticImage)
    } else if (complicationData is SmallImageComplicationData) {
        fixImages(context, complicationData.smallImage)
    }
}

fun fixImages(context: Context, smallImage: SmallImage?) {
    fixImage(context, smallImage?.image)
    fixImage(context, smallImage?.ambientImage)
}

fun fixImages(context: Context, monochromaticImage: MonochromaticImage?) {
    fixImage(context, monochromaticImage?.image)
    fixImage(context, monochromaticImage?.ambientImage)
}

@SuppressLint("SoonBlockedPrivateApi", "NewApi")
fun fixImage(context: Context, image: Icon?) {
    if (image?.type == Icon.TYPE_RESOURCE) {
        // Icon uses PackageManager to find resources for the target app
        // but fails in preview tooling with layoutlib
        val field = Icon::class.java.getDeclaredField("mObj1")
        field.isAccessible = true
        field.set(image, context.resources)
    }
}
