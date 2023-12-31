import SwiftUI
import ConfettiKit

struct VenueView: View {
    private let component: VenueComponent

    @StateValue
    private var uiState: VenueComponentUiState
    
    init(_ component: VenueComponent) {
        self.component = component
        _uiState = StateValue(component.uiState)
    }
    
    var body: some View {
        VStack {
            switch uiState {
            case is VenueComponentLoading: ProgressView()
            case is VenueComponentError: ErrorView()
            case let state as VenueComponentSuccess: VenueContentView(component: component, uiState: state)
            default: EmptyView()
            }
        }
        .navigationBarTitle("Venue", displayMode: .inline)
    }
}

private struct VenueContentView: View {
    let component: VenueComponent
    let uiState: VenueComponentSuccess
    
    var body: some View {
        
        
        ScrollView {
            VStack(spacing: 16) {
                Text(uiState.data.name).font(.title)
                let mapLink = uiState.data.mapLink
                let address = uiState.data.address
                if (mapLink != nil && address != nil) {
                    Text(address!)
                        .font(.subheadline)
                        .underline()
                        .onTapGesture {
                            UIApplication.shared.open(URL(string: mapLink!)!)
                        }
                } else {
                         Text(uiState.data.address ?? "").font(.subheadline)
                     }
                Text(uiState.data.description_)
                
                if let imageUrl = uiState.data.imageUrl {
                    AsyncImage(url: URL(string: imageUrl)) { image in
                        image.resizable()
                            .aspectRatio(contentMode: .fit)
                    } placeholder: {
                        ProgressView()
                    }
                    .frame(height: 200)
                }

                if let floorPlanUrl = uiState.data.floorPlanUrl {
                    AsyncImage(url: URL(string: floorPlanUrl)) { image in
                        image.resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        ProgressView()
                    }
                }

            }
        }
    }
    
}

