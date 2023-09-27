import SwiftUI
import ConfettiKit

// Source: https://github.com/arkivanov/Decompose/blob/master/sample/app-ios/app-ios/DecomposeHelpers/ObservableValue.swift
public class ObservableValue<T : AnyObject> : ObservableObject {
    private let observableValue: Value<T>

    @Published
    var value: T

    private var cancellation: Cancellation?
    
    init(_ value: Value<T>) {
        observableValue = value
        self.value = observableValue.value
        cancellation = observableValue.observe { [weak self] value in self?.value = value }
    }

    deinit {
        cancellation?.cancel()
    }
}
