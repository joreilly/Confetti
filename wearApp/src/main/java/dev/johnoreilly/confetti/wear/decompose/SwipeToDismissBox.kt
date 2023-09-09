@file:OptIn(ExperimentalWearFoundationApi::class)

package dev.johnoreilly.confetti.wear.decompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.OnFocusChange
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.SwipeToDismissKeys
import androidx.wear.compose.material.TimeText
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScalingLazyColumnState
import com.google.android.horologist.compose.layout.scrollAway

/**
 * Displays the [ChildStack] in [SwipeToDismissBox][androidx.wear.compose.material.SwipeToDismissBox].
 *
 * @param stack a [ChildStack] to be displayed.
 * @param onDismissed called when the swipe to dismiss gesture has completed, allows popping the stack.
 * See [StackNavigator#pop][com.arkivanov.decompose.router.stack.pop].
 * @param modifier a [Modifier] to be applied to the underlying
 * [SwipeToDismissBox][androidx.wear.compose.material.SwipeToDismissBox].
 * @param content a Composable slot displaying a [Child][Child.Created].
 */
@Composable
fun <C : Any, T : Any> SwipeToDismissBox(
    stack: Value<ChildStack<C, T>>,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable SwipeToDismissBoxScope.(child: Child.Created<C, T>) -> Unit,
) {
    val state = stack.subscribeAsState()

    SwipeToDismissBox(
        stack = state.value,
        onDismissed = onDismissed,
        modifier = modifier,
        content = content,
    )
}

class SwipeToDismissBoxScope {
    private val _scrollableState = mutableStateOf<ScalingLazyColumnState?>(null)

    val scrollableState: ScalingLazyColumnState?
        get() = _scrollableState.value

    @Composable
    fun createScalingLazyColumnState(
        factory: ScalingLazyColumnState.Factory = ScalingLazyColumnDefaults.belowTimeText()
    ): ScalingLazyColumnState =
        factory.create().also {
            _scrollableState.value = it
        }
}

/**
 * Displays the [ChildStack] in [SwipeToDismissBox][androidx.wear.compose.material.SwipeToDismissBox].
 *
 * @param stack a [ChildStack] to be displayed.
 * @param onDismissed called when the swipe to dismiss gesture has completed, allows popping the stack.
 * See [StackNavigator#pop][com.arkivanov.decompose.router.stack.pop].
 * @param modifier a [Modifier] to be applied to the underlying
 * [SwipeToDismissBox][androidx.wear.compose.material.SwipeToDismissBox].
 * @param content a Composable slot displaying a [Child][Child.Created].
 */
@Composable
fun <C : Any, T : Any> SwipeToDismissBox(
    stack: ChildStack<C, T>,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable SwipeToDismissBoxScope.(child: Child.Created<C, T>) -> Unit,
) {
    val active: Child.Created<C, T> = stack.active
    val background: Child.Created<C, T>? = stack.backStack.lastOrNull()
    val holder = rememberSaveableStateHolder()

    RetainStates(holder, stack.getConfigurations())

    val activeScalingLazyColumnState = remember { mutableStateOf<ScalingLazyColumnState?>(null) }

    Scaffold(
        timeText = {
            val timeTextModifier = remember {
                derivedStateOf {
                    val scrollableState = activeScalingLazyColumnState.value

                    if (scrollableState != null) {
                        Modifier.scrollAway(scrollableState)
                    } else {
                        Modifier
                    }
                }
            }
            TimeText(modifier = timeTextModifier.value)
        }
    ) {
        SwipeToDismissBox(
            onDismissed = onDismissed,
            modifier = modifier,
            backgroundKey = background?.configuration ?: SwipeToDismissKeys.Background,
            contentKey = active.configuration,
            hasBackground = background != null,
        ) { isBackground ->
            val scope = remember { SwipeToDismissBoxScope() }
            if (!isBackground) {
                OnFocusChange { isFocused ->
                    if (isFocused) {
                        activeScalingLazyColumnState.value = scope.scrollableState
                    }
                }
            }
            val child = background?.takeIf { isBackground } ?: active
            holder.SaveableStateProvider(child.configuration.key()) {
                Scaffold(
                    positionIndicator = {
                        val scrollState = scope.scrollableState
                        if (scrollState != null) {
                            PositionIndicator(scrollState.state)
                        }
                    }
                ) {
                    scope.content(child)
                }
            }
        }
    }
}

private fun ChildStack<*, *>.getConfigurations(): Set<String> =
    items.mapTo(HashSet()) { it.configuration.key() }

private fun Any.key(): String = "${this::class.simpleName}_${hashCode().toString(radix = 36)}"

@Composable
private fun RetainStates(holder: SaveableStateHolder, currentKeys: Set<String>) {
    val keys = remember(holder) { Keys(currentKeys) }

    DisposableEffect(holder, currentKeys) {
        keys.set.forEach {
            if (it !in currentKeys) {
                holder.removeState(it)
            }
        }

        keys.set = currentKeys

        onDispose {}
    }
}

private class Keys(var set: Set<String>)
