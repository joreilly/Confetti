import SwiftUI
import Combine
import ConfettiKit
import KMMViewModelCore
import KMMViewModelSwiftUI


struct ConfettiApp: View {

    @StateViewModel var viewModel = AppViewModel()

    init() {
        UITabBar.appearance().backgroundColor = UIColor.systemBackground
    }
        
    var body: some View {
        
        if let conference = viewModel.conference, !conference.isEmpty {
            ConferenceView(conference: conference) {
                Task {
                    do {
                        try await viewModel.setConference(conference: "")
                    }
                    catch {
                        print(error)
                    }
                }
             }
        } else {
            ConferenceListView{ conference in
                Task {
                    do {
                        try await viewModel.setConference(conference: conference)
                    }
                    catch {
                        print(error)
                    }
                }
             }
        }
    }
}

struct ConferenceView: View {
    
    let conference: String
    let navigateToConferences: () -> Void
    @State var selectedSession: SessionDetails?
    @StateViewModel var viewModel:SessionsViewModel
    
    init(conference: String, navigateToConferences: @escaping () -> Void) {
        self.conference = conference
        self.navigateToConferences = navigateToConferences
        self._viewModel = StateViewModel(wrappedValue: SessionsViewModel(conference: conference, uid: nil, tokenProvider: nil))
    }

    var body: some View {
        VStack {
            switch viewModel.uiState {
            case let uiState as SessionsUiStateSuccess:
                
                TabView {
                    SessionsView(conference: conference, sessionUiState: uiState, navigateToConferences: navigateToConferences)
                    .tabItem {
                        Label("Schedule", systemImage: "calendar")
                    }
                    SpeakerListView(speakerList: uiState.speakers)
                        .tabItem {
                            Label("Speakers", systemImage: "person")
                        }
                }
            default:
                ProgressView()
            }
        }
    }
}


struct ConferenceListView: View {
    @StateViewModel var viewModel = ConferencesViewModel()

    let navigateToSessions: (String) -> Void
    
    init(navigateToSessions: @escaping (String) -> Void) {
        self.navigateToSessions = navigateToSessions
    }
    
    var body: some View {
        return NavigationView {
            switch viewModel.uiState {
            case let uiState as ConferencesViewModel.Success:
                List(uiState.conferences, id: \.self) { conference in
                    HStack {
                        Text(conference.name)
                        Spacer()
                        Text("\(conference.days[0])")
                    }.onTapGesture(
                        perform: {
                            navigateToSessions(conference.id)
                        }
                    )
                }
                .navigationTitle("Choose conference")
                .navigationBarBackButtonHidden(true)
            case _ as ConferencesViewModel.Error:
                Text("Something went wrong")
            default:
                ProgressView()
            }
        }
    }
}


struct SessionsView: View {

    let conference: String
    let sessionUiState: SessionsUiStateSuccess
    let navigateToConferences: () -> Void

    @State private var selectedSession: SessionDetails?
    @StateViewModel var viewModel:SessionsViewModel
    
    init(conference: String, sessionUiState: SessionsUiStateSuccess, navigateToConferences: @escaping () -> Void) {
        self.conference = conference
        self.sessionUiState = sessionUiState
        self.navigateToConferences = navigateToConferences
        self._viewModel = StateViewModel(wrappedValue: SessionsViewModel(conference: conference, uid: nil, tokenProvider: nil))
    }
    
    var body: some View {
        
        NavigationSplitView {
            VStack {
                SessionListView(viewModel: viewModel, sessionUiState: sessionUiState, navigateToConferences: navigateToConferences, refresh: {
                    do {
                        try await viewModel.refresh()
                    } catch {
                        print("Failed with error: \(error)")
                    }
                }, selectedSession: $selectedSession)
            }
            .navigationSplitViewColumnWidth(ideal: 400)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    Text(sessionUiState.conferenceName)
                        .multilineTextAlignment(.center).bold()
                }
            }
            .navigationBarItems(
                  trailing: Button(action: {
                      navigateToConferences()
                  }, label: {
                      Text("Switch")
                  }))

        } detail: {
            if let selectedSession {
                SessionDetailsView(session: selectedSession)
            }
        }
    }
}

extension SessionDetails: Identifiable { }
extension SpeakerDetails: Identifiable { }


extension Color {
  init(_ hex: UInt, alpha: Double = 1) {
    self.init(
      .sRGB,
      red: Double((hex >> 16) & 0xFF) / 255,
      green: Double((hex >> 8) & 0xFF) / 255,
      blue: Double(hex & 0xFF) / 255,
      opacity: alpha
    )
  }
}

