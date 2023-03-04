import SwiftUI
import ConfettiKit
import KMMViewModelSwiftUI


struct ContentView: View {
    
    var body: some View {
        SessionListScreen()
    }
}

struct SessionListScreen: View {
    @StateViewModel var viewModel = ConfettiViewModel()
    
    var body: some View {
        VStack {
            switch viewModel.uiState {
            case let uiState as SessionsUiStateSuccess:
                SessionListView(sessionUiState: uiState)
            default:
                ProgressView()
            }
        }
    }
}

struct SessionListView: View {
    var sessionUiState: SessionsUiStateSuccess
    
    var body: some View {
        NavigationView {
            List {
                let sessionsMap = sessionUiState.sessionsByStartTimeList[0]
                
                ForEach(sessionsMap.keys.sorted(), id: \.self) {key in
                    ForEach(sessionsMap[key] ?? [], id: \.self) { session in
                        SessionView(session: session)
                    }
                }
            }
            .navigationBarTitle(Text(sessionUiState.conferenceName))
        }
    }
}

struct SessionView: View {
    var session: SessionDetails
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(session.title)
            Text(session.sessionSpeakerInfo()).font(.system(size: 14)).bold()
        }
    }
}
