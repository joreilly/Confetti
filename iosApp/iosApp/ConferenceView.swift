import SwiftUI
import ConfettiKit

struct ConferenceView: View {
    private let component: ConferenceComponent
    
    @StateValue
    private var stack: ChildStack<AnyObject, ConferenceComponentChild>
    
    init(_ component: ConferenceComponent) {
        self.component = component
        _stack = StateValue(component.stack)
    }
    
    var body: some View {
        HomeView(component)
/*
        StackView(
            stackValue: ObservableValue(component.stack),
            onBack: component.onBackClicked
        ) { child in
            switch child {
            case let child as ConferenceComponentChild.Home: HomeView(child.component)
            //case let child as ConferenceComponentChild.SessionDetails: SessionDetailsView(child.component)
            case let child as ConferenceComponentChild.SpeakerDetails: SpeakerDetailsView(child.component)
            default: EmptyView()
            }
        }
 */
    }
}
