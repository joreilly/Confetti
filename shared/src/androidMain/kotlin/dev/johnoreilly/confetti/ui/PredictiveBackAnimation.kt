package dev.johnoreilly.confetti.ui

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.PredictiveBackParams
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.materialPredictiveBackAnimatable
import com.arkivanov.essenty.backhandler.BackHandler

@ExperimentalDecomposeApi
internal actual fun <C : Any, T : Any> predictiveBackAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit,
): StackAnimation<C, T> =
    stackAnimation(
        animator = fade() + scale(),
        predictiveBackParams = {
            PredictiveBackParams(
                backHandler = backHandler,
                onBack = onBack,
                animatable = ::materialPredictiveBackAnimatable,
            )
        },
    )
