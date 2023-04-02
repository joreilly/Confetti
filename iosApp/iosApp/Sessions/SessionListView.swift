import SwiftUI
import ConfettiKit
import KMMViewModelCore
import KMMViewModelSwiftUI

struct SessionListView: View {
    @ObservedObject var viewModel: SessionsViewModel
    var sessionUiState: SessionsUiStateSuccess
    let navigateToConferences: () -> Void
    let refresh: () async -> Void
    @State var selectedDateIndex: Int = 0
    
        
    var body: some View {
        NavigationView {
            VStack {

                Picker(selection: $selectedDateIndex, label: Text("Date")) {
                    ForEach(0..<sessionUiState.formattedConfDates.count, id: \.self) { i in
                        Text("\(sessionUiState.formattedConfDates[i])").tag(i)
                    }
                }
                .pickerStyle(.segmented)

                List {
                    ForEach(sessionUiState.sessionsByStartTimeList[selectedDateIndex].keys.sorted(), id: \.self) {key in
                        
                        Section(header: HStack {
                            Image(systemName: "clock")
                            Text(key).font(.headline)
                          }) {

                                                        
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
            .listStyle(.plain)
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
            .searchable(text: $viewModel.searchQuery)
        }
    }
}


struct SessionView: View {
    var session: SessionDetails
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(session.title).font(.headline)
            if session.room != nil {
                Text(session.sessionSpeakers() ?? "").font(.subheadline)
                Text(session.room?.name ?? "").font(.subheadline).foregroundColor(.gray)
            }
        }
    }
}




