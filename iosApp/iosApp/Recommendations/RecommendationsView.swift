import SwiftUI
import MarkdownUI
import ConfettiKit

struct RecommendationsView: View {
    private let component: RecommendationsComponent

    @StateValue
    private var uiState: RecommendationsComponentUiState
    
    @State private var query: String = ""
    
    init(_ component: RecommendationsComponent) {
        self.component = component
        _uiState = StateValue(component.uiState)
    }

    var body: some View {
        VStack(spacing: 16) {
            TextField("What topic are you interested in?", text: $query,
                onCommit: {
                    if (!query.isEmpty) {
                        component.makeQuery(query: query)
                    }
                }
            )
            .border(.secondary)
            .textFieldStyle(RoundedBorderTextFieldStyle())
            .padding(.horizontal, 16)
            .padding(.vertical, 16)

            switch uiState {
            case is RecommendationsComponentLoading: ProgressView()
            case is RecommendationsComponentError: ErrorView()
            case let state as RecommendationsComponentSuccess: RecommendationsContentView(component: component, uiState: state)
            default: EmptyView()
            }
            
            Spacer()
        }
        .navigationBarTitle("Recommendations", displayMode: .inline)
    }
}

private struct RecommendationsContentView: View {
    let component: RecommendationsComponent
    let uiState: RecommendationsComponentSuccess

    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            List {
                ForEach(uiState.data.recommendedSessions, id: \.self) {session in
                    SessionView(session: session)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            component.onSessionClicked(id: session.id)
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
        }
    }
}
