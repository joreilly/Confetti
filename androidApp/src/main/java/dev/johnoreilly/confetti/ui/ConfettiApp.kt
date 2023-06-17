package dev.johnoreilly.confetti.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation
import dev.johnoreilly.confetti.decompose.AppComponent
import dev.johnoreilly.confetti.decompose.AppComponent.Child
import dev.johnoreilly.confetti.conferences.ConferencesRoute

@Composable
fun ConfettiApp(
    component: AppComponent,
    windowSizeClass: WindowSizeClass,
) {
    Children(
        stack = component.stack,
        animation = stackAnimation(fade()),
    ) {
        when (val child = it.instance) {
            is Child.Loading -> LoadingView()
            is Child.Conferences -> ConferencesRoute(child.component)
            is Child.Conference -> ConferenceRoute(child.component, windowSizeClass)
        }
    }
}
