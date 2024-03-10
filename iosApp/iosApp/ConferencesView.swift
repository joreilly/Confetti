import SwiftUI
import ConfettiKit

struct ConferencesView: View {
    private let component: ConferencesComponent
    
    @StateValue
    private var uiState: ConferencesComponentUiState
    
    init(_ component: ConferencesComponent) {
        self.component = component
        _uiState = StateValue(component.uiState)
    }
    
    var body: some View {
        VStack  {
            switch uiState {
            case let uiState as ConferencesComponentSuccess:
                ConferenceListContentViewShared(conferences: uiState.conferenceListByYear) { conference in
                    component.onConferenceClicked(conference: conference)
                }
            case is ConferencesComponentError: ErrorView()
            default: ProgressView()
            }
        }
    }
}


private struct ConferenceListContentViewShared: UIViewControllerRepresentable {
    let conferences: [KotlinInt : [GetConferencesQuery.Conference]]
    let onConferenceClick: (GetConferencesQuery.Conference) -> Void
    
    func makeUIViewController(context: Context) -> UIViewController {
        return SharedViewControllersKt.ConferenceListViewController(conferenceListByYear: conferences, onConferenceClick: onConferenceClick)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}


private struct SessionView: View {
    var session: SessionDetails
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(session.title).font(.headline)
            if session.room != nil {
                Text(session.sessionSpeakers() ?? "").font(.subheadline)
                Text(session.room?.name ?? "").font(.subheadline).foregroundColor(.gray)
            }
            if session.isLightning() {
                Text("Lightning / \(session.startsAt.time)-\(session.endsAt.time)")
                    .colorInvert()
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.primary)
                    .cornerRadius(8)
            }
        }
    }
}
