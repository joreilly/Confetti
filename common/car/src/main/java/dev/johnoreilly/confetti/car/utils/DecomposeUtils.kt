package dev.johnoreilly.confetti.car.utils

import androidx.car.app.Screen
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.essentyLifecycle

fun Screen.defaultComponentContext(): ComponentContext =
    DefaultComponentContext(
        lifecycle = essentyLifecycle(),
    )
