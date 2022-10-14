import SwiftUI
import ConfettiKit

struct SessionListView: View {
    @ObservedObject var viewModel: ConfettiViewModel

    let gradient = Gradient(colors: [Color(0xFFEBFB), Color(0xFFEDE6)])
    
    var body: some View {
        NavigationView {
            VStack {
                Spacer().frame(height: 16)

                switch viewModel.uiState {
                case .success(let confDates, _, let sessions):
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
                    Text("Sessions").font(.largeTitle.bold())
                }
            }
            .scrollContentBackground(.hidden)
            .background {
                LinearGradient(gradient: gradient, startPoint: .top, endPoint: .bottom)
                    .edgesIgnoringSafeArea(.vertical)
            }
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
            Spacer().frame(height: 8)
            Text(viewModel.getSessionSpeakerLocation(session: session)).font(.system(size: 14))
            Spacer()
        }
    }
}




