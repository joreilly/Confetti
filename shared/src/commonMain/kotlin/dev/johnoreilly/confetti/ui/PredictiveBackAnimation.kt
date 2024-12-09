package dev.johnoreilly.confetti.ui

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimation
import com.arkivanov.essenty.backhandler.BackHandler

@ExperimentalDecomposeApi
internal expect fun <C : Any, T : Any> predictiveBackAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit,
): StackAnimation<C, T>
