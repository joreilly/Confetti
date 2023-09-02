package dev.johnoreilly.confetti.wear.decompose


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.SwipeToDismissBoxState
import androidx.wear.compose.material.SwipeToDismissKeys
import com.arkivanov.decompose.Child.Created
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState

@Composable
fun DecomposeNavHost(
    component: WearAppComponent,
    content: @Composable (Created<Any, Any>) -> Unit
) {
    val stack by component.stack.subscribeAsState()
    val previous = stack.backStack.lastOrNull()
    val current = stack.active

    val state = SwipeToDismissBoxState()

    SwipeToDismissBox(
        state = state,
        backgroundKey = previous?.configuration ?: SwipeToDismissKeys.Background,
        contentKey = current.configuration,
        hasBackground = previous != null,
        onDismissed = { component.navigateUp() }
    ) { isBackground ->
        val configuration: Created<Any, Any>? = if (isBackground) {
            previous
        } else {
            current
        }

        if (configuration != null) {
            content(configuration)
        }
    }
}