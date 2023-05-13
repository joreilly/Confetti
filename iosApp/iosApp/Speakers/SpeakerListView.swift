import SwiftUI
import ConfettiKit

struct SpeakersView: View {
    private let component: SpeakersComponent

    @StateValue
    private var uiState: SpeakersUiState
    
    init(_ component: SpeakersComponent) {
        self.component = component
        _uiState = StateValue(component.uiState)
    }
    
    var body: some View {
        VStack {
            switch uiState {
            case is SpeakersUiStateLoading: ProgressView()
            case is SpeakersUiStateError: ErrorView()
            case let state as SpeakersUiStateSuccess: SpeakersContentView(component: component, uiState: state)
            default: EmptyView()
            }
        }
        .navigationBarTitle("Speakers", displayMode: .inline)
    }
}

private struct SpeakersContentView: View {
    let component: SpeakersComponent
    let uiState: SpeakersUiStateSuccess
    
    var body: some View {
        NavigationView {
            List(uiState.speakers, id: \.self) { speaker in
                SpeakerView(speaker: speaker)
                    .onTapGesture { component.onSpeakerClicked(id: speaker.id) }
            }
        }
    }
}

struct SpeakerView: View {
    var speaker: SpeakerDetails
    
    var body: some View {
        HStack {
            if let image = speaker.photoUrl,
               let url = URL(string: image) {
                AsyncImage(url: url) { image in
                    image.resizable()
                } placeholder: {
                    ProgressView()
                }
                .frame(width: 50, height: 50)
                .clipShape(RoundedRectangle(cornerRadius: 25))
            }
            VStack(alignment: .leading) {
                Text(speaker.name).font(.headline)
                Text(speaker.tagline ?? "").font(.subheadline)
            }
        }
    }
}

