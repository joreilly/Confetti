import SwiftUI
import ConfettiKit
import KMMViewModelCore
import KMMViewModelSwiftUI

struct SessionListView: View {
    var sessionUiState: SessionsUiStateSuccess
    let navigateToConferences: () -> Void
    let refresh: () async -> Void
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
                                VStack {
                                    if (!session.isBreak()) {
                                        NavigationLink(destination: SessionDetailsView(session: session)) {
                                            SessionView(session: session)
                                        }
                                    } else {
                                        SessionView(session: session)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    Text(sessionUiState.conferenceName).font(.largeTitle.bold())
                }
            }
            .navigationBarItems(
                  trailing: Button(action: {
                      navigateToConferences()
                  }, label: {
                      Text("Switch")
                  }))
            .refreshable {
                await refresh()
            }
        }
    }
}


struct SessionView: View {
    var session: SessionDetails
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(session.title)
            if session.room != nil {
                Spacer().frame(height: 8)
                Text(session.sessionSpeakerLocation()).font(.system(size: 14)).bold()
            }
            Spacer()
        }
    }
}




