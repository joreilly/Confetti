package dev.johnoreilly.confetti.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.FaultyDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.isFront
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import dev.johnoreilly.confetti.decompose.ConferenceComponent
import dev.johnoreilly.confetti.decompose.ConferenceComponent.Child
import dev.johnoreilly.confetti.sessiondetails.SessionDetailsRoute
import dev.johnoreilly.confetti.settings.SettingsRoute
import dev.johnoreilly.confetti.speakerdetails.SpeakerDetailsRoute

@OptIn(ExperimentalDecomposeApi::class, FaultyDecomposeApi::class)
@Composable
fun ConferenceRoute(
    component: ConferenceComponent,
    windowSizeClass: WindowSizeClass,
) {
    ConferenceMaterialTheme(component.conferenceThemeColor) {
        Children(
            stack = component.stack,
            animation = predictiveBackAnimation(
                backHandler = component.backHandler,
                fallbackAnimation = stackAnimation { _, _, direction ->
                    if (direction.isFront) {
                        slide() + fade()
                    } else {
                        scale(frontFactor = 1F, backFactor = 0.7F) + fade()
                    }
                },
                onBack = component::onBackClicked,
            ),
        ) {
            when (val child = it.instance) {
                is Child.Home -> HomeRoute(child.component, windowSizeClass)
                is Child.SessionDetails -> SessionDetailsRoute(child.component)
                is Child.SpeakerDetails -> SpeakerDetailsRoute(child.component)
                is Child.Settings -> {
                    child.component?.let { childComponent ->
                        SettingsRoute(childComponent, component::onBackClicked)
                    }
                }
            }
        }

    }
}
