package dev.johnoreilly.confetti.livery.wear

import androidx.compose.remote.creation.compose.action.Action
import androidx.compose.remote.creation.compose.action.hostAction
import androidx.compose.remote.creation.compose.state.RemoteBoolean
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteInt
import androidx.compose.remote.creation.compose.state.RemoteString
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.ui.graphics.Color
import ee.schimke.livery.capture.ContractChecks
import ee.schimke.livery.protocol.Contract

/**
 * A Wear design's named values and actions, checked against its [contract] as Livery's
 * `LiveryValues` checks them (livery-capture-core's [ContractChecks]), for remote-m3-catalog's
 * port of AndroidX's creation API: `LiveryValues` is typed to rc-players' API, and this port is in
 * AndroidX's package. A name the contract doesn't declare, or the wrong type, fails the capture.
 * Defaults are the contract's, so the document draws them where the app leaves a value out.
 */
class WearValues(val contract: Contract) {
    private val checks = ContractChecks(contract)

    fun string(name: String): RemoteString = RemoteString.createNamedRemoteString(name, checks.string(name))

    fun int(name: String): RemoteInt = RemoteInt.createNamedRemoteInt(name, checks.int(name))

    fun bool(name: String): RemoteBoolean = RemoteBoolean.createNamedRemoteBoolean(name, checks.bool(name))

    /** A colour input or a `theme.*` role; [fallback] draws until the host binds one. */
    fun color(name: String, fallback: Color): RemoteColor =
        RemoteColor.createNamedRemoteColor(name, checks.color(name)?.let(::Color) ?: fallback)

    /** An action the contract declares, with no argument. */
    fun action(name: String): Action {
        checks.actionPayload(name)
        return hostAction(name.rs)
    }

    /**
     * An action whose one argument [arg] is a value of the document: its payload is
     * `{"<arg>":"<value>"}`, built as the document plays (Livery maps a bare value only to `value`).
     */
    fun action(name: String, arg: String, value: RemoteString): Action {
        checks.actionPayload(name, mapOf(arg to "x"))
        return hostAction(name.rs, "{\"$arg\":\"".rs + value + "\"}".rs)
    }
}
