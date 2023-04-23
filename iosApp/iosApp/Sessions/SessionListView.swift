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
    @Binding var selectedSession: SessionDetails?
        
    var body: some View {
        VStack {

            let formattedConfDates = sessionUiState.formattedConfDates
            Picker(selection: $selectedDateIndex, label: Text("Date")) {
                ForEach(0..<formattedConfDates.count, id: \.self) { i in
                    Text("\(formattedConfDates[i])").tag(i)
                }
            }
            .pickerStyle(.segmented)

            List(selection: $selectedSession) {
                ForEach(sessionUiState.sessionsByStartTimeList[selectedDateIndex].keys.sorted(), id: \.self) {key in
                    
                    Section(header: HStack {
                        Image(systemName: "clock")
                        Text(key).font(.headline).bold()
                    }) {
                                                    
                        let sessions = sessionUiState.sessionsByStartTimeList[selectedDateIndex][key] ?? []
                        ForEach(sessions, id: \.self) { session in
                            SessionView(session: session)
                        }
                    }
                    
                }
            }
        }
        .listStyle(.insetGrouped)
        .refreshable {
            await refresh()
        }
        .searchable(text: $viewModel.searchQuery)
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




