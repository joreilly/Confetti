import SwiftUI
import ConfettiKit


struct SpeakerDetailsView: View {
    var component: SpeakerDetailsComponent

    @StateValue
    private var uiState: SpeakerDetailsUiState
    
    init(_ component: SpeakerDetailsComponent) {
        self.component = component
        _uiState = StateValue(component.uiState)
    }

    var body: some View {
        VStack {
            switch uiState {
            case is SpeakerDetailsUiState.Loading: ProgressView()
            case is SpeakerDetailsUiState.Error: ErrorView()
            case let state as SpeakerDetailsUiState.Success:
                SpeakerDetailsContentView(component: component, speaker: state.details)
            default: EmptyView()
            }
        }.navigationBarTitle("Speaker", displayMode: .inline)
    }
}

private struct SpeakerDetailsContentView: View {
    let component: SpeakerDetailsComponent
    let speaker: SpeakerDetails
    
    var body: some View {
        ScrollView {
            VStack(alignment: .center) {
                Text(speaker.name).font(.title).textSelection(.enabled)
                Text(speaker.tagline ?? "").font(.subheadline).textSelection(.enabled)

                AsyncImage(url: URL(string: speaker.photoUrl ?? "")) { image in
                     image.resizable()
                        .aspectRatio(contentMode: .fit)
                } placeholder: {
                    ProgressView()
                }
                .frame(width: 240, height: 240)
                .clipShape(RoundedRectangle(cornerRadius: 16))

                Spacer().frame(height: 16)
                Text(speaker.bio ?? "").font(.body).textSelection(.enabled)
                Spacer()

                HStack {
                    SessionSpeakerSocialInfo(speaker: speaker)
                }
                Spacer()
                Text("Sessions").font(.headline)
                ForEach(speaker.sessions, id: \.self) { session in
                    Text(session.title)
                        .font(.body)
                        .onTapGesture { component.onSessionClicked(id: session.id) }
                }
            }
            .padding()
        }
    }
}
