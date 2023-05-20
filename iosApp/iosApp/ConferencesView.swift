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
            switch uiState {
            case let uiState as ConferencesComponentSuccess:
                List(uiState.conferences, id: \.self) { conference in
                    HStack {
                        Text(conference.name)
                        Spacer()
                        Text("\(conference.days[0])")
                    }.onTapGesture {
                        component.onConferenceClicked(conference: conference.id)
                    }
                }
                .navigationTitle("Choose conference")
                .navigationBarBackButtonHidden(true)
            case is ConferencesComponentError: ErrorView()
            default: ProgressView()
            }
        }
    }
}
