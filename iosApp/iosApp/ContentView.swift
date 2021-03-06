import SwiftUI
import Combine
import shared


struct ContentView: View {
    @StateObject var viewModel = ConfettiViewModel()

    var body: some View {
        TabView {
            SessionListView(viewModel: viewModel)
                .tabItem {
                    Label("Sessions", systemImage: "film")
                }
            SpeakerListView(viewModel: viewModel)
                .tabItem {
                    Label("Speakers", systemImage: "person")
                }
            RoomListView(viewModel: viewModel)
                .tabItem {
                    Label("Rooms", systemImage: "location")
                }

        }
    }
}

struct SessionListView: View {
    @ObservedObject var viewModel: ConfettiViewModel

    var body: some View {
                NavigationView {
            List(viewModel.sessions) { session in
                NavigationLink(destination: SessionDetailsView(session: session)) {
                    VStack(alignment: .leading) {
                        SessionView(viewModel: viewModel, session: session)
                    }
                }
            }
            .navigationTitle("Sessions")
            // TODO need to figure out how we want to generally handle languages
//            .toolbar {
//                ToolbarItem(placement: .automatic) {
//                    LanguageMenu(viewModel: viewModel)
//                }
//            }
            .task {
                await viewModel.observeSessions()
            }
        }
    }
}


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


struct SessionView: View {
    @ObservedObject var viewModel: ConfettiViewModel
    var session: SessionDetails

    var body: some View {
        VStack(alignment: .leading) {
            Text(viewModel.getSessionTime(session: session))
            Spacer()
            Text(session.title).font(.headline).foregroundColor(.blue)
            Spacer()
            Text(viewModel.getSessionSpeakerLocation(session: session))
        }
    }
}

struct SessionDetailsView: View {
    var session: SessionDetails

    var body: some View {
        
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(session.title).font(.title).foregroundColor(.blue)
                Divider()
                
                Text(session.sessionDescription())
                ScrollView(.horizontal, showsIndicators: false) {
                    LazyHStack(alignment: .center) {
                        ForEach(session.tags, id: \.self) { tag in
                            Text(tag)
                                .padding(.vertical, 10)
                                .padding(.horizontal)
                                .background(.blue)
                                .foregroundColor(.white)
                                .background(Capsule().stroke())
                                .clipShape(Capsule())
                        }
                    }
                    .padding(.vertical)
                }
                
                ForEach(session.speakers, id: \.self) { speaker in
                    Text(speaker.name).bold()
                    Text(speaker.bio)
                }
                Spacer()
            }
            .padding()
        }
    }
}


struct SpeakerListView: View {
    @ObservedObject var viewModel: ConfettiViewModel

    var body: some View {
        NavigationView {
            List(viewModel.speakers) { speaker in
                VStack(alignment: .leading) {
                    SpeakerView(speaker: speaker)
                }
            }
            .navigationTitle("Speakers")
            .task {
                await viewModel.observeSpeakers()
            }
        }
    }
}


struct SpeakerView: View {
    var speaker: SpeakerDetails
    
    var body: some View {
        HStack {
            if let image = speaker.imageUrl(),
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
                Text(speaker.company ?? "").font(.subheadline)
            }
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


