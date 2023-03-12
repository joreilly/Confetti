import SwiftUI
import Combine
import ConfettiKit
import KMMViewModelCore
import KMMViewModelSwiftUI


struct ContentView: View {
    @ObservedViewModel var viewModel: ConfettiViewModel

    init(viewModel: ObservableViewModel<ConfettiViewModel>.Projection) {
        self._viewModel = ObservedViewModel(viewModel)
        UITabBar.appearance().backgroundColor = UIColor.white
    }
        
    var body: some View {
        if (viewModel.savedConference.isEmpty) {
            ConferenceListView(viewModel: $viewModel) {
                viewModel.setConference(conference: "")
            }
        } else {
            ConferenceView(viewModel: $viewModel, conference: viewModel.savedConference) {
                viewModel.setConference(conference: "")
            }
        }
    }
}


struct ConferenceListView: View {
    @ObservedViewModel var viewModel: ConfettiViewModel
    let showConferenceList: () -> Void
    
    init(viewModel: ObservableViewModel<ConfettiViewModel>.Projection, showConferenceList: @escaping () -> Void) {
        self._viewModel = ObservedViewModel(viewModel)
        self.showConferenceList = showConferenceList
    }
    
    var body: some View {
        NavigationView {
            List(viewModel.conferenceList, id: \.self) { conference in
                NavigationLink(destination: ConferenceView(viewModel: $viewModel, conference: conference.id, showConferenceList: showConferenceList).navigationBarBackButtonHidden(true)) {
                    HStack {
                        Text(conference.name)
                        Spacer()
                        Text("\(conference.days[0])")
                    }
                }

            }
            .navigationTitle("Choose conference")
            .navigationBarBackButtonHidden(true)
        }
    }
}


struct ConferenceView: View {
    @ObservedViewModel var viewModel: ConfettiViewModel
    let conference: String
    let showConferenceList: () -> Void
    
    
    init(viewModel: ObservableViewModel<ConfettiViewModel>.Projection, conference: String, showConferenceList: @escaping () -> Void) {
        self._viewModel = ObservedViewModel(viewModel)
        self.conference = conference
        self.showConferenceList = showConferenceList
    }

    var body: some View {
        VStack {
            switch viewModel.uiState {
            case let uiState as SessionsUiStateSuccess:
                
                TabView {
                    SessionListView(sessionUiState: uiState, showConferenceList: showConferenceList, refresh: {
                        do {
                            try await viewModel.refresh()
                        } catch {
                            print("Failed with error: \(error)")
                        }
                    })
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
        .onAppear {
            viewModel.setConference(conference: conference)
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

