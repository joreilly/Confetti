import SwiftUI
import UIKit
import ConfettiKit

// Source: https://github.com/arkivanov/Decompose/blob/master/sample/app-ios/app-ios/DecomposeHelpers/StackView.swift
struct StackView<T: AnyObject, Content: View>: View {
    @ObservedObject
    var stackValue: ObservableValue<ChildStack<AnyObject, T>>
    
    var onBack: (_ newCount: Int32) -> Void
    
    @ViewBuilder
    var childContent: (T) -> Content
    
    var stack: [Child<AnyObject, T>] { stackValue.value.items }
    
    var body: some View {
        NavigationStack(
            path: Binding(
                get: { stack.dropFirst() },
                set: { updatedPath in onBack(Int32(updatedPath.count)) }
            )
        ) {
            childContent(stack.first!.instance!)
                .navigationDestination(for: Child<AnyObject, T>.self) {
                    childContent($0.instance!)
                }
        }
    }
}
