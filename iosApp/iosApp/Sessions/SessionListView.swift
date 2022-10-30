import SwiftUI
import ConfettiKit

struct SessionListView: View {
    @ObservedObject var viewModel: ConfettiViewModel
    let showConferenceList: () -> Void
    
    var body: some View {
        NavigationView {
            VStack {
                Spacer().frame(height: 16)

                switch viewModel.uiState {
                case .success(_, let confDates, _, let sessions):
                    Picker(selection: $viewModel.selectedDateIndex, label: Text("Date")) {
                        ForEach(0..<confDates.count, id: \.self) { i in
                            Text("\(confDates[i])").tag(i)
                        }
                    }
                    .pickerStyle(.segmented)

                    List(sessions) { session in
                        VStack {
                            if (!session.isBreak()) {
                                NavigationLink(destination: SessionDetailsView(session: session)) {
                                    SessionView(viewModel: viewModel, session: session)
                                }
                            } else {
                                SessionView(viewModel: viewModel, session: session)
                            }
                        }
                        .listRowBackground(Color.clear)
                        .listRowSeparator(.hidden)
                    }
                case .loading:
                    ProgressView()
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    if case  .success(let conferenceName, _, _, _) = viewModel.uiState {
                        Text(conferenceName).font(.largeTitle.bold())
                    } else {
                        Text("")
                    }
                }
            }
            .navigationBarItems(
                  trailing: Button(action: {
                      showConferenceList()
                  }, label: {
                      Text("Switch")
                  }))
            .scrollContentBackground(.hidden)
            .background(Color(0xF6F6F6))
            .refreshable {
                await viewModel.refresh()
            }
        }
        
    }
}


struct SessionView: View {
    @ObservedObject var viewModel: ConfettiViewModel
    var session: SessionDetails

    var body: some View {
        VStack(alignment: .leading) {
            Text(viewModel.getSessionTime(session: session)).bold()
            Spacer()
            Text(session.title)
            if session.room != nil {
                Spacer().frame(height: 8)
                Text(viewModel.getSessionSpeakerLocation(session: session)).font(.system(size: 14)).bold()
            }
            Spacer()
        }
    }
}




