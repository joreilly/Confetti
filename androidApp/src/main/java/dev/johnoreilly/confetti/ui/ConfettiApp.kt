package dev.johnoreilly.confetti.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import dev.johnoreilly.confetti.decompose.AppComponent
import dev.johnoreilly.confetti.decompose.AppComponent.Child

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
            is Child.Conferences -> ConferenceListView(child.component)
            is Child.Conference -> ConferenceRoute(child.component, windowSizeClass)
        }
    }
}
