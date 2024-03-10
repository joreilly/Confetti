import SwiftUI
import SwiftUIFlowLayout
import ConfettiKit


struct SessionDetailsView: View {
    private let component: SessionDetailsComponent
    private let conferenceThemeColor: String?
    @Environment(\.openURL) var openURL

    @StateValue
    private var uiState: SessionDetailsUiState
    
    init(_ component: SessionDetailsComponent, _ conferenceThemeColor: String?) {
        self.component = component
        self.conferenceThemeColor = conferenceThemeColor
        _uiState = StateValue(component.uiState)
    }

    var body: some View {
        VStack {
            switch uiState {
            case is SessionDetailsUiState.Loading: ProgressView()
            case is SessionDetailsUiState.Error: ErrorView()
            case let state as SessionDetailsUiState.Success:
                //SessionDetailsContentView(component: component, session: state.sessionDetails)
                SessionDetailsContentViewShared(
                    session: state.sessionDetails,
                    conferenceThemeColor: conferenceThemeColor ?? "",
                    onSpeakerClick: { speakerId in
                        component.onSpeakerClicked(id: speakerId)
                    },
                    onSocialLinkClicked: { urlString in
                        if let url = URL(string: urlString) {
                            openURL(url)
                        }
                    }
                )
            default: EmptyView()
            }
        }.navigationBarTitle("Session", displayMode: .inline)
    }
}


// This version is using Compose for iOS....
private struct SessionDetailsContentViewShared: UIViewControllerRepresentable {
    let session: SessionDetails
    let conferenceThemeColor: String
    let onSpeakerClick: (String) -> Void
    let onSocialLinkClicked: (String) -> Void
    
    func makeUIViewController(context: Context) -> UIViewController {
        return SharedViewControllersKt.SessionDetailsViewController(session: session,
            conferenceThemeColor: conferenceThemeColor,
            onSpeakerClick: onSpeakerClick, onSocialLinkClicked: onSocialLinkClicked)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

private struct SessionDetailsContentView : View {
    let component: SessionDetailsComponent
    let session: SessionDetails

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 8) {
                Text(session.title).font(.title).foregroundColor(.blue).textSelection(.enabled)
                Spacer()
                
                Text(session.sessionDescription ?? "").font(.body).textSelection(.enabled)
                                
                if session.tags.count > 0 {
                    FlowLayout(mode: .scrollable,
                               items: session.tags,
                               itemSpacing: 4) {
                        Text($0)
                            .padding(.vertical, 10)
                            .padding(.horizontal)
                            .background(.blue)
                            .foregroundColor(.white)
                            .background(Capsule().stroke())
                            .clipShape(Capsule())
                    }
                }
                
                Spacer()
                ForEach(session.speakers, id: \.self) { speaker in
                    SessionSpeakerInfo(speaker: speaker.speakerDetails)
                        .onTapGesture { component.onSpeakerClicked(id: speaker.id) }
                }
                Spacer()
            }
            .padding()
        }
    }
}




//#Preview("Preview 1") {
//    NavigationView {
//        VStack {
//            let session = MockDataKt.sessionDetails
//            SessionDetailsContentViewShared(session: session, onSpeakerClick: {_ in }, onSocialLinkClicked: {_ in })
//        }.navigationBarTitle("Session 1", displayMode: .inline)
//    }
//}

