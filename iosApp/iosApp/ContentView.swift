import SwiftUI
import Combine
import ConfettiKit
import KMMViewModelCore
import KMMViewModelSwiftUI




struct ContentView: View {
    
    var body: some View {
        ConferenceView()
    }
}


struct ConferenceView: View {
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
    @State var selectedDateIndex: Int = 0
    
        
    var body: some View {
        NavigationView {
            VStack {
                Spacer().frame(height: 16)

                Picker(selection: $selectedDateIndex, label: Text("Date")) {
                    ForEach(0..<sessionUiState.confDates.count, id: \.self) { i in
                        Text("\(sessionUiState.confDates[i])").tag(i)
                    }
                }
                .pickerStyle(.segmented)

                List {
                    ForEach(sessionUiState.sessionsByStartTimeList[selectedDateIndex].keys.sorted(), id: \.self) {key in
                        
                        Section(header: Text(key).foregroundColor(Color("Title"))) {
                            let sessions = sessionUiState.sessionsByStartTimeList[selectedDateIndex][key] ?? []
                            ForEach(sessions, id: \.self) { session in
                                SessionView(session: session)
                            }
                        }
                    }
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    Text(sessionUiState.conferenceName)
                }
            }
        }
    }
}


struct SessionView: View {
    var session: SessionDetails
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(session.title)
            Text(session.sessionSpeakerLocation()).font(.system(size: 14)).bold()
        }
    }
}





