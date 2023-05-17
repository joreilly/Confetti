import SwiftUI
import Combine
import ConfettiKit

struct ConfettiApp: View {
    private let component: AppComponent
    
    @StateValue
    private var stack: ChildStack<AnyObject, AppComponentChild>
    
    init(_ component: AppComponent) {
        self.component = component
        _stack = StateValue(component.stack)
        
        UITabBar.appearance().backgroundColor = UIColor.systemBackground
    }
    
    var body: some View {
        switch stack.active.instance {
        case is AppComponentChild.Loading: ProgressView()
        case let child as AppComponentChild.Conferences: ConferencesView(child.component)
        case let child as AppComponentChild.Conference: ConferenceView(child.component)
        default: EmptyView()
        }
    }
}
