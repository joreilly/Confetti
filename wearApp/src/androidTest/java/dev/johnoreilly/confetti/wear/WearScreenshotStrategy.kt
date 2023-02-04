/*
 * Copyright 2022 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.johnoreilly.confetti.wear

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.core.graphics.applyCanvas
import androidx.test.platform.app.InstrumentationRegistry
import tools.fastlane.screengrab.ScreenshotCallback
import tools.fastlane.screengrab.ScreenshotStrategy
import kotlin.math.min

class WearScreenshotStrategy(val isRoundDevice: Boolean) : ScreenshotStrategy {
    override fun takeScreenshot(screenshotName: String, screenshotCallback: ScreenshotCallback) {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        var screenshot = uiAutomation.takeScreenshot()

        if (isRoundDevice) {
            screenshot = circularClip(screenshot)
        }

        screenshotCallback.screenshotCaptured(screenshotName, screenshot)
    }

    fun circularClip(image: Bitmap): Bitmap {
        // From https://github.com/coil-kt/coil/blob/2.0.0-rc01/coil-base/src/main/java/coil/transform/CircleCropTransformation.kt
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val minSize = min(image.width, image.height)
        val radius = minSize / 2f
        val output = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        output.applyCanvas {
            drawCircle(radius, radius, radius, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            drawBitmap(image, radius - image.width / 2f, radius - image.height / 2f, paint)
            drawColor(Color.BLACK, PorterDuff.Mode.DST_OVER)
        }

        return output
    }
}