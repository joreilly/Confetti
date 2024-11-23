package dev.johnoreilly.confetti.ui

import androidx.compose.ui.window.ComposeUIViewController
import dev.johnoreilly.confetti.decompose.DefaultAppComponent

fun MainViewController(component: DefaultAppComponent) = ComposeUIViewController {

    App(component)
}