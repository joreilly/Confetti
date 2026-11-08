package dev.johnoreilly.confetti.livery

import androidx.compose.remote.creation.compose.action.RemoteAction
import androidx.compose.remote.creation.compose.action.hostAction
import androidx.compose.remote.creation.compose.state.RemoteBoolean
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteImageBitmap
import androidx.compose.remote.creation.compose.state.RemoteString
import androidx.compose.remote.creation.compose.state.rememberNamedRemoteBoolean
import androidx.compose.remote.creation.compose.state.rememberNamedRemoteColor
import androidx.compose.remote.creation.compose.state.rememberNamedRemoteString
import androidx.compose.remote.creation.compose.state.rememberRemoteImageBitmap
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * The contract a template is being captured for, and what it touched: the inputs it reads and the
 * actions it emits. Livery wants both in the template's `<target>.json` (`reads`, `emits`);
 * recording them at capture time means they can't drift from the document, and anything outside
 * the contract fails the capture.
 */
class TemplateScope(val contract: ConfettiContract, val brand: ConferenceBrand? = null) {
    val reads = linkedSetOf<String>()
    val emits = linkedSetOf<String>()
    /** Font families the document names: the sidecar's `fonts`, so their files download with it. */
    val fonts = linkedSetOf<String>()

    /** The family labels use, recorded as used. */
    fun font(): String? = brand?.fontFamily?.also { fonts += it }

    fun read(input: Input) {
        check(contract.inputs[input.name] === input) { "${input.name} is not an input of ${contract.ref}" }
        reads += input.name
    }

    fun emit(action: Action) {
        check(contract.actions[action.name] === action) { "${action.name} is not an action of ${contract.ref}" }
        emits += action.name
    }
}

val LocalTemplateScope = staticCompositionLocalOf<TemplateScope> { error("not capturing a template") }

/**
 * A contract string input, as a `USER` named value (PROTOCOL §4.2). Its initial value is the
 * contract's default, never example data: a host pushes only the inputs it binds, so an optional
 * input the app leaves out keeps the initial value, and an example there (a ticket URL, a
 * speaker's name) would show up as real data.
 */
@Composable
fun input(input: Input): RemoteString {
    val scope = LocalTemplateScope.current
    require(input.type == InputType.String) { "${input.name} is ${input.type}" }
    scope.read(input)
    return rememberNamedRemoteString(input.name, input.default?.jsonPrimitive?.contentOrNull ?: "")
}

@Composable
fun inputBool(input: Input): RemoteBoolean {
    val scope = LocalTemplateScope.current
    require(input.type == InputType.Bool) { "${input.name} is ${input.type}" }
    scope.read(input)
    val initial = input.default?.jsonPrimitive?.booleanOrNull ?: false
    return rememberNamedRemoteBoolean(input.name, initial)
}

/** A colour input: a theme role (`theme.primary`) a theme pack can override; the brand is the initial value. */
@Composable
fun inputColor(input: Input, initial: Color): RemoteColor {
    require(input.type == InputType.Color) { "${input.name} is ${input.type}" }
    LocalTemplateScope.current.read(input)
    return rememberNamedRemoteColor(input.name, initial)
}

/** An image input, drawn from `livery:value/<input>` (PROTOCOL §6.1); the client resolves it. */
@Composable
fun inputImage(input: Input): RemoteImageBitmap {
    require(input.type == InputType.Image) { "${input.name} is ${input.type}" }
    LocalTemplateScope.current.read(input)
    return rememberRemoteImageBitmap("livery:value/${input.name}")
}

/** A contract action with no argument. */
@Composable
fun emit(action: Action): RemoteAction {
    require(action.arg == null) { "${action.name} takes ${action.arg}" }
    LocalTemplateScope.current.emit(action)
    return hostAction(action.name.rs)
}

/**
 * A contract action with its one string argument. Livery maps a bare text value only to the
 * argument `value`, so any other argument is sent as a JSON object built in the document.
 */
@Composable
fun emit(action: Action, value: RemoteString): RemoteAction {
    val arg = requireNotNull(action.arg) { "${action.name} takes no argument" }
    LocalTemplateScope.current.emit(action)
    return hostAction(action.name.rs, if (arg == "value") value else "{\"$arg\":\"".rs + value + "\"}")
}

/*
 * Context parameters (Kotlin 2.4, no flag): a design declares the contract it is written against
 * as `context(t: ContractScope<SpeakerContract>)`, and reads inputs and emits actions by picking
 * from that contract (`text { name }`, `action { open }`). Inside the picker `this` is the
 * contract, so `text { title }` in a speaker design doesn't compile. The checks `input` and `emit`
 * make at capture time stay as a backstop (e.g. `text { SessionContract.title }` still fails
 * there).
 */

/** The contract and brand a design is written for, provided by the catalog entry that captures it. */
class ContractScope<out C : ConfettiContract>(val contract: C, val brand: ConferenceBrand)

/** A string input of this design's contract. */
context(t: ContractScope<C>)
@Composable
fun <C : ConfettiContract> text(pick: C.() -> Input): RemoteString = input(t.checked().pick())

/** A boolean input of this design's contract. */
context(t: ContractScope<C>)
@Composable
fun <C : ConfettiContract> flag(pick: C.() -> Input): RemoteBoolean = inputBool(t.checked().pick())

/** An image input of this design's contract. */
context(t: ContractScope<C>)
@Composable
fun <C : ConfettiContract> image(pick: C.() -> Input): RemoteImageBitmap = inputImage(t.checked().pick())

/** An action of this design's contract, with no argument. */
context(t: ContractScope<C>)
@Composable
fun <C : ConfettiContract> action(pick: C.() -> Action): RemoteAction = emit(t.checked().pick())

/** An action of this design's contract with its one argument, [value] (`tickets`'s url). */
context(t: ContractScope<C>)
@Composable
fun <C : ConfettiContract> action(pick: C.() -> Action, value: RemoteString): RemoteAction = emit(t.checked().pick(), value)

/** The brand's colours, as the theme roles a theme pack overrides. */
context(t: ContractScope<C>)
@Composable
fun <C : ConfettiContract> colors(): BrandColors = brandColors(t.brand)

/** The scope's contract, once checked against the template being captured. */
@Composable
private fun <C : ConfettiContract> ContractScope<C>.checked(): C {
    val capturing = LocalTemplateScope.current.contract
    check(capturing === contract) { "a ${contract.ref} design is being captured as ${capturing.ref}" }
    return contract
}
