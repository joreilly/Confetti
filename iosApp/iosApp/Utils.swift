import ConfettiKit

func awaitForState<T: AnyObject>(_ state: Value<T>, predicate: (T) -> Bool) async {
    repeat {
        try? await Task.sleep(for: .milliseconds(500))
    } while (!predicate(state.value))
}
