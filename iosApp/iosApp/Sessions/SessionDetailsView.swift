import SwiftUI
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
                SessionDetailsContentViewShared(
                    conference: state.conference,
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
    let conference: String
    let session: SessionDetails
    let conferenceThemeColor: String
    let onSpeakerClick: (String) -> Void
    let onSocialLinkClicked: (String) -> Void
    
    func makeUIViewController(context: Context) -> UIViewController {
        return SharedViewControllersKt.SessionDetailsViewController(
            conference: conference,
            session: session,
            conferenceThemeColor: conferenceThemeColor,
            onSpeakerClick: onSpeakerClick, onSocialLinkClicked: onSocialLinkClicked)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
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

