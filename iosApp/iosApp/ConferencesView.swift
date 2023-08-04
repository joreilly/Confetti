import SwiftUI
import ConfettiKit

struct ConferencesView: View {
    private let component: ConferencesComponent
    
    @StateValue
    private var uiState: ConferencesComponentUiState
    
    init(_ component: ConferencesComponent) {
        self.component = component
        _uiState = StateValue(component.uiState)
    }
    
    var body: some View {
        return NavigationView {
            VStack  {
                switch uiState {
                case let uiState as ConferencesComponentSuccess:
                    ConferencesByYearView(component: component, conferencesUiState: uiState)
                case is ConferencesComponentError: ErrorView()
                default: ProgressView()
                }
            }.navigationBarTitle("Confetti", displayMode: .inline)
        }
    }
}

private struct ConferencesByYearView: View {
    let component: ConferencesComponent
    let conferencesUiState: ConferencesComponentSuccess

    var body: some View {
        VStack {
            let conferencesByYear = conferencesUiState.conferenceListByYear

            List {
                ForEach(Array(conferencesByYear.keys).sorted { $0.intValue > $1.intValue }, id: \.self) { year in
                    
                    Section(header: HStack {
                        Text(year.stringValue).font(.headline).bold()
                    }) {
                        let conferences = conferencesUiState.conferenceListByYear[year] ?? []
                        ForEach(conferences, id: \.self) { conference in
                            HStack {
                                Text(conference.name)
                                Spacer()
                                Text("\(conference.days[0])")
                            }.onTapGesture {
                                component.onConferenceClicked(conference: conference)
                            }

                        }
                    }
                }
            }
        }
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
