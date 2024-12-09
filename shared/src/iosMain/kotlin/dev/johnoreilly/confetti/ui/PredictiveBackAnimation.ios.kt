package dev.johnoreilly.confetti.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.*
import com.arkivanov.decompose.extensions.compose.stack.animation.isFront
import com.arkivanov.essenty.backhandler.BackHandler

@ExperimentalDecomposeApi
internal actual fun <C : Any, T : Any> predictiveBackAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit,
): StackAnimation<C, T> =
    stackAnimation(
        animator = iosLikeSlide(),
        predictiveBackParams = {
            PredictiveBackParams(
                backHandler = backHandler,
                onBack = onBack,
            )
        },
    )

@ExperimentalDecomposeApi
private fun iosLikeSlide(): StackAnimator =
    stackAnimator { factor, direction ->
        Modifier
            .then(if (direction.isFront) Modifier else Modifier.fade(factor + 1F))
            .offsetXFactor(factor = if (direction.isFront) factor else factor * 0.5F)
    }

private fun Modifier.fade(factor: Float) =
    drawWithContent {
        drawContent()
        drawRect(color = Color(red = 0F, green = 0F, blue = 0F, alpha = (1F - factor) / 4F))
    }

private fun Modifier.offsetXFactor(factor: Float): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        layout(placeable.width, placeable.height) {
            placeable.placeRelative(x = (placeable.width.toFloat() * factor).toInt(), y = 0)
        }
    }
