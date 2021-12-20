import SwiftUI
import Combine
import shared


struct ContentView: View {
    @StateObject var viewModel = KikiConfViewModel()

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
    @ObservedObject var viewModel: KikiConfViewModel

    var body: some View {
        NavigationView {
            List(viewModel.sessions, id: \.id) { session in
                NavigationLink(destination: SessionDetailsView(session: session)) {
                    VStack(alignment: .leading) {
                        Text(session.title).font(.headline)
                    }
                }
            }
            .navigationTitle("Sessions")
            .onAppear {
                viewModel.startObservingSessions()
            }
            .onDisappear {
                viewModel.stopObservingSessions()
            }
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
    @ObservedObject var viewModel: KikiConfViewModel

    var body: some View {
        NavigationView {
            List(viewModel.speakers, id: \.id) { speaker in
                VStack(alignment: .leading) {
                    SpeakerView(speaker: speaker)
                }
            }
            .navigationTitle("Speakers")
            .onAppear {
                viewModel.startObservingSpeakers()
            }
            .onDisappear {
                viewModel.stopObservingSpeakers()
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
                Text(speaker.company ?? "").font(.subheadline)
            }
        }
    }
}


struct RoomListView: View {
    @ObservedObject var viewModel: KikiConfViewModel

    var body: some View {
        NavigationView {
            List(viewModel.rooms, id: \.id) { room in
                VStack(alignment: .leading) {
                    Text(room.name).font(.headline)
                }
            }
            .navigationTitle("Rooms")
            .onAppear {
                viewModel.startObservingRooms()
            }
            .onDisappear {
                viewModel.stopObservingRooms()
            }

        }
    }
}


