import SwiftUI
import ConfettiKit

struct SessionsView: View {
    private let component: SessionsComponent

    @StateValue
    private var uiState: SessionsUiState
    
    init(_ component: SessionsComponent) {
        self.component = component
        _uiState = StateValue(component.uiState)
    }
    
    var body: some View {
        switch uiState {
        case is SessionsUiStateLoading: ProgressView()
        case is SessionsUiStateError: ErrorView()
        case let state as SessionsUiStateSuccess: SessionsContentView(component: component, sessionUiState: state)
        default: EmptyView()
        }
    }
}

private struct SessionsContentView: View {
    let component: SessionsComponent
    let sessionUiState: SessionsUiStateSuccess
    @State private var selectedDateIndex: Int = 0

    var body: some View {
        VStack {
            let formattedConfDates = sessionUiState.formattedConfDates
            Picker(selection: $selectedDateIndex, label: Text("Date")) {
                ForEach(0..<formattedConfDates.count, id: \.self) { i in
                    Text("\(formattedConfDates[i])").tag(i)
                }
            }
            .pickerStyle(.segmented)
            .padding([.leading, .trailing], 16)
            .padding(.bottom, 8)

            List {
                ForEach(sessionUiState.sessionsByStartTimeList[selectedDateIndex].keys.sorted(), id: \.self) {key in
                    
                    Section(header: HStack {
                        Image(systemName: "clock")
                        Text(key).font(.headline).bold()
                    }) {
                                                    
                        let sessions = sessionUiState.sessionsByStartTimeList[selectedDateIndex][key] ?? []
                        ForEach(sessions, id: \.self) { session in
                            SessionView(session: session)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .contentShape(Rectangle())
                                .onTapGesture { component.onSessionClicked(id: session.id) }
                                .listRowBackground(session.id == sessionUiState.selectedSessionId ? Color(.systemFill) : Color(uiColor: .systemBackground))
                        }
                    }
                    
                }
            }
        }
        .listStyle(.insetGrouped)
        .refreshable {
            component.refresh()
            
            await awaitForState(component.uiState) { state in
                (state as? SessionsUiStateSuccess)?.isRefreshing == true
            }
        }
        .searchable(text: Binding(get: { sessionUiState.searchString }, set: component.onSearch))
        .navigationBarTitle(sessionUiState.conferenceName, displayMode: .inline)
    }
}


private struct SessionView: View {
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
