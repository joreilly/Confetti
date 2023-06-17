package dev.johnoreilly.confetti.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.isFront
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation
import dev.johnoreilly.confetti.decompose.ConferenceComponent
import dev.johnoreilly.confetti.decompose.ConferenceComponent.Child
import dev.johnoreilly.confetti.account.SignInRoute
import dev.johnoreilly.confetti.sessiondetails.SessionDetailsRoute
import dev.johnoreilly.confetti.settings.SettingsRoute
import dev.johnoreilly.confetti.speakerdetails.SpeakerDetailsRoute

@Composable
fun ConferenceRoute(
    component: ConferenceComponent,
    windowSizeClass: WindowSizeClass,
) {
    Children(
        stack = component.stack,
        animation = stackAnimation { _, _, direction ->
            if (direction.isFront) {
                slide() + fade()
            } else {
                scale(frontFactor = 1F, backFactor = 0.7F) + fade()
            }
        },
    ) {
        when (val child = it.instance) {
            is Child.Home -> HomeRoute(child.component, windowSizeClass)
            is Child.SessionDetails -> SessionDetailsRoute(child.component)
            is Child.SpeakerDetails -> SpeakerDetailsRoute(child.component)
            is Child.SignIn -> SignInRoute(child.component)
            is Child.Settings -> SettingsRoute(child.component)
        }
    }
}
