import SwiftUI
import ConfettiKit

struct ConferenceView: View {
    private let component: ConferenceComponent
    private let userLoggedIn: Bool
    
    @StateValue
    private var stack: ChildStack<AnyObject, ConferenceComponentChild>
    
    init(_ component: ConferenceComponent, _ userLoggedIn: Bool) {
        self.component = component
        self.userLoggedIn = userLoggedIn
        _stack = StateValue(component.stack)
    }
    
    var body: some View {
        StackView(
            stackValue: ObservableValue(component.stack),
            onBack: component.onBackClicked
        ) { child in
            switch child {
            case let child as ConferenceComponentChild.Home: HomeView(child.component, userLoggedIn)
            case let child as ConferenceComponentChild.SessionDetails: SessionDetailsView(child.component, component.conferenceThemeColor)
            case let child as ConferenceComponentChild.SpeakerDetails: SpeakerDetailsView(child.component)
            default: EmptyView()
            }
        }
    }
}
