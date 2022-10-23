import SwiftUI
import Combine
import ConfettiKit


struct ContentView: View {
    @StateObject var viewModel = ConfettiViewModel()

    init() {
        UITabBar.appearance().backgroundColor = UIColor.white
    }
    
    var body: some View {
        NavigationView {
            List(viewModel.conferenceList, id: \.self) { conference in
                NavigationLink(destination: ConferenceView(viewModel: viewModel, conference: conference.id).navigationBarBackButtonHidden(true)) {
                    Text(conference.name)
                }
                
            }
            .navigationTitle("Choose conference")
            .navigationBarBackButtonHidden(true)
        }
    }
}


struct ConferenceView: View {
    @ObservedObject var viewModel: ConfettiViewModel
    let conference: String

    var body: some View {
        TabView {
            SessionListView(viewModel: viewModel)
                .tabItem {
                    Label("Schedule", systemImage: "calendar")
                }
            SpeakerListView(viewModel: viewModel)
                .tabItem {
                    Label("Speakers", systemImage: "person")
                }
        }
        .onAppear {
            viewModel.setConference(conference: conference)
        }
    }
}




// TODO need to figure out how we want to generally handle languages
//            .toolbar {
//                ToolbarItem(placement: .automatic) {
//                    LanguageMenu(viewModel: viewModel)
//                }
//            }




struct LanguageMenu: View {
    @ObservedObject var viewModel: ConfettiViewModel

    @State var languages: [String] = ["French", "English"]
    @State var selections: [String] = []

    
    var body: some View {
        Menu {
            ForEach(self.languages, id: \.self) { language in
                Button(action: {
                    viewModel.toggleLanguageChecked(language: language)
                }) {
                    HStack {
                        Label {
                            Text(language)
                        } icon: {
                            if (viewModel.enabledLanguages.contains(language)) {
                                Image(systemName: "checkmark")
                            }
                        }
                    }
                }
            }
        } label: {
            Text("Filter")
        }
    }
}



struct RoomListView: View {
    @ObservedObject var viewModel: ConfettiViewModel

    var body: some View {
        NavigationView {
            List(viewModel.rooms) { room in
                VStack(alignment: .leading) {
                    Text(room.name).font(.headline)
                }
            }
            .navigationTitle("Rooms")
            .task {
                await viewModel.observeRooms()
            }
        }
    }
}

extension Color {
  init(_ hex: UInt, alpha: Double = 1) {
    self.init(
      .sRGB,
      red: Double((hex >> 16) & 0xFF) / 255,
      green: Double((hex >> 8) & 0xFF) / 255,
      blue: Double(hex & 0xFF) / 255,
      opacity: alpha
    )
  }
}

