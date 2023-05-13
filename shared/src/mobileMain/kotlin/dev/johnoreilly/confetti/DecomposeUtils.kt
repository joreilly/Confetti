package dev.johnoreilly.confetti

import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun LifecycleOwner.coroutineScope(
    context: CoroutineContext = Dispatchers.Main.immediate,
): CoroutineScope {
    val scope = CoroutineScope(context + SupervisorJob())
    lifecycle.doOnDestroy(scope::cancel)

    return scope
}

internal fun <T : Any> StateFlow<T>.asValue(
    context: CoroutineContext = Dispatchers.Main.immediate,
): Value<T> =
    object : Value<T>() {
        override val value: T get() = this@asValue.value
        private val observers = HashMap<(T) -> Unit, CoroutineScope>()

        override fun subscribe(observer: (T) -> Unit) {
            require(observer != observers)
            val scope = CoroutineScope(context)
            observers[observer] = scope
            scope.launch { collect(observer) }
        }

        override fun unsubscribe(observer: (T) -> Unit) {
            observers.remove(observer)?.cancel()
        }
    }

internal fun <T : Any> Flow<T>.asValue(
    initialValue: T,
    scope: CoroutineScope,
): Value<T> =
    stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = initialValue,
    ).asValue(scope.coroutineContext)
