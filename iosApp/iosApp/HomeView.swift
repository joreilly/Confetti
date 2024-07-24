import SwiftUI
import ConfettiKit

struct HomeView: View {
    private let component: HomeComponent
    
    @StateValue
    private var stack: ChildStack<AnyObject, HomeComponentChild>
    
    init(_ component: HomeComponent) {
        self.component = component
        _stack = StateValue(component.stack)
    }
    
    var body: some View {
        VStack {
            let child = stack.active.instance
            
            ChildView(child: child)
                .frame(maxHeight: .infinity)
            
            HStack(alignment: .bottom, spacing: 16) {
                BottomTabView(
                    title: "Schedule",
                    systemImage: "calendar",
                    isActive: child is HomeComponentChild.Sessions,
                    action: component.onSessionsTabClicked
                ).frame(maxWidth: .infinity)

                BottomTabView(
                    title: "Speakers",
                    systemImage: "person",
                    isActive: child is HomeComponentChild.Speakers,
                    action: component.onSpeakersTabClicked
                ).frame(maxWidth: .infinity)

                BottomTabView(
                    title: "Venue",
                    systemImage: "location",
                    isActive: child is HomeComponentChild.Venue,
                    action: component.onVenueTabClicked
                ).frame(maxWidth: .infinity)

            }
        }
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Menu {
                    Button("Switch Conference", action: component.onSwitchConferenceClicked)
                    if (component.isGeminiEnabled()) {
                        Button("Recommendations", action: component.onGetRecommendationsClicked)
                    }
                } label: {
                    Image(systemName: "gearshape")
                }
            }
        }
    }
}

private struct ChildView: View {
    let child: HomeComponentChild
    
    var body: some View {
        switch child {
        case let child as HomeComponentChild.Sessions: SessionsView(child.component)
        case let child as HomeComponentChild.MultiPane: MultiPaneView(child.component)
        case let child as HomeComponentChild.Speakers: SpeakersView(child.component)
        case let child as HomeComponentChild.Venue: VenueView(child.component)
        case let child as HomeComponentChild.Recommendations: RecommendationsView(child.component)
        default: EmptyView()
        }
    }
}

private struct BottomTabView: View {
    let title: String
    let systemImage: String
    let isActive: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Label(title, systemImage: systemImage)
                .labelStyle(VerticalLabelStyle())
                .opacity(isActive ? 1 : 0.5)
        }
    }
}

private struct VerticalLabelStyle: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        VStack(alignment: .center, spacing: 8) {
            configuration.icon
            configuration.title
        }
    }
}
