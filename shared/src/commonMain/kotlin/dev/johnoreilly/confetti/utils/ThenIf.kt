package dev.johnoreilly.confetti.utils

import androidx.compose.ui.Modifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

public inline fun Modifier.thenIf(
    condition: Boolean,
    block: Modifier.() -> Modifier,
): Modifier {
    return if (condition) then(block(Modifier)) else this
}

@OptIn(ExperimentalContracts::class)
public inline fun <T> Modifier.thenNotNull(
    value: T?,
    block: Modifier.(T) -> Modifier,
): Modifier {
    contract { returns() implies (value != null) }
    return if (value != null) then(block(Modifier, value)) else this
}
